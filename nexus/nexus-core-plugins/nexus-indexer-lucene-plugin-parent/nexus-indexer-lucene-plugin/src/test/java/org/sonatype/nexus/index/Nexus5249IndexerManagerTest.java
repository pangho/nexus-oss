package org.sonatype.nexus.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.Nullable;

import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdater;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.util.CompositeException;

import com.google.common.base.Predicate;

/**
 * Test for NEXUS-5249 and related ones (see linked issues). In general, we ensure that 404 happened during remote
 * update does not break the batch-processing of ALL repositories (task should not stop and should go on process other
 * repositories). Also, 401/403/50x errors will throw IOException at the processng end (and hence, make the task
 * failed), but again, the batch is not broken due to one repo being broken, the exceptions are supressed until batch
 * end.
 * <p>
 * {@link DefaultIndexerManager} "reindex" operation (invoked multiple times by "all" operations tested here) actually
 * does two things: first, if needed (repo is proxy, index download enabled), will try to update index from remote
 * (using {@link IndexUpdater#fetchAndUpdateIndex(IndexUpdateRequest)} component, and then, will invoke
 * {@link NexusIndexer#scan(IndexingContext, String, ArtifactScanningListener, boolean)}. This UT assumes this order of
 * invocation, and that following conditions are true:
 * <ul>
 * <li>if {@link IndexUpdater#fetchAndUpdateIndex(IndexUpdateRequest)} hits HTTP 404 response, no interruption of
 * execution happens at all (not for given repo being processed, nor for the "all" operation), this was the bug
 * NEXUS-5249</li>
 * <li>if if {@link IndexUpdater#fetchAndUpdateIndex(IndexUpdateRequest)} hits any other unexpected HTTP response other
 * than 404, no interruption of "all" operation happens (but currently processed repository is stopped from being
 * processed, no scan is invoked), and the "all" operation should fail at end</li>
 * <li></li>
 * </ul>
 * 
 * @author cstamas
 */
public class Nexus5249IndexerManagerTest
    extends AbstractIndexerManagerTest
{
    protected int indexedRepositories;

    protected int indexedProxyRepositories;

    protected MavenProxyRepository failingRepository;

    protected FailingInvocationHandler fetchFailingInvocationHandler;

    protected CountingInvocationHandler fetchCountingInvocationHandler;

    protected CountingInvocationHandler scanCountingInvocationHandler;

    protected void prepare( final IOException failure )
        throws Exception
    {
        // count total of indexed, and total of indexed proxy repositories, and set the one to make it really go remotely
        indexedRepositories = 0;
        indexedProxyRepositories = 0;
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( !repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class )
                && !repository.getRepositoryKind().isFacetAvailable( GroupRepository.class )
                && repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) && repository.isIndexable() )
            {
                indexedRepositories++;
            }
            if ( repository.getId().equals( apacheSnapshots.getId() ) )
            {
                final MavenProxyRepository mavenProxyRepository = repository.adaptToFacet( MavenProxyRepository.class );
                mavenProxyRepository.setDownloadRemoteIndexes( true );
                mavenProxyRepository.commitChanges();
                failingRepository = mavenProxyRepository;
                indexedProxyRepositories++;
            }
        }

        // faking IndexUpdater that will fail with given exception for given "failing" repository
        final IndexUpdater realUpdater = lookup( IndexUpdater.class );
        // predicate to match invocation when the arguments are for the failingRepository
        final Predicate<Object[]> fetchAndUpdateIndexMethodArgumentsPredicate = new Predicate<Object[]>()
        {
            @Override
            public boolean apply( @Nullable Object[] input )
            {
                if ( input != null )
                {
                    final IndexUpdateRequest req = (IndexUpdateRequest) input[0];
                    if ( req != null )
                    {
                        return req.getIndexingContext().getId().startsWith( failingRepository.getId() );
                    }
                }
                return false;
            }
        };
        // method we want to fail and count
        final Method fetchAndUpdateIndexMethod =
            IndexUpdater.class.getMethod( "fetchAndUpdateIndex", new Class[] { IndexUpdateRequest.class } );
        fetchFailingInvocationHandler =
            new FailingInvocationHandler( new PassThruInvocationHandler( realUpdater ), fetchAndUpdateIndexMethod,
                fetchAndUpdateIndexMethodArgumentsPredicate, failure );
        fetchCountingInvocationHandler =
            new CountingInvocationHandler( fetchFailingInvocationHandler, fetchAndUpdateIndexMethod );
        final IndexUpdater fakeUpdater =
            (IndexUpdater) Proxy.newProxyInstance( getClass().getClassLoader(), new Class[] { IndexUpdater.class },
                fetchCountingInvocationHandler );

        // faking NexusIndexer, invoked by tested IndexerManager to perform scans only to count scan invocations
        final NexusIndexer realIndexer = lookup( NexusIndexer.class );
        scanCountingInvocationHandler =
            new CountingInvocationHandler( new PassThruInvocationHandler( realIndexer ), NexusIndexer.class.getMethod(
                "scan", new Class[] { IndexingContext.class, String.class, ArtifactScanningListener.class,
                    boolean.class } ) );
        final NexusIndexer fakeIndexer =
            (NexusIndexer) Proxy.newProxyInstance( getClass().getClassLoader(), new Class[] { NexusIndexer.class },
                scanCountingInvocationHandler );

        // applying faked components
        final DefaultIndexerManager dim = (DefaultIndexerManager) indexerManager;
        dim.setIndexUpdater( fakeUpdater );
        dim.setNexusIndexer( fakeIndexer );
    }

    @Test
    public void remote404ResponseDoesNotFailsProcessing()
        throws Exception
    {
        // HTTP 404 pops up as FileNotFoundEx
        prepare( new FileNotFoundException( "fluke" ) );

        try
        {
            // reindex all
            indexerManager.reindexAllRepositories( null, false );

            // we continue here as 404 should not end up with exception (is "swallowed")

            // ensure we fetched from one we wanted (failingRepository)
            Assert.assertEquals( indexedProxyRepositories, fetchCountingInvocationHandler.getInvocationCount() );
            // ensure we scanned all the repositories, even the failing one, having 404 on remote update
            Assert.assertEquals( indexedRepositories, scanCountingInvocationHandler.getInvocationCount() );
        }
        catch ( IOException e )
        {
            Assert.fail( "There should be no exception thrown!" );
        }
    }

    @Test
    public void remoteNon404ResponseFailsProcessingAtTheEnd()
        throws Exception
    {
        // HTTP 401/403/etc boils down as some other IOException
        final IOException ex = new IOException( "something bad happened" );
        prepare( ex );

        try
        {
            // reindex all
            indexerManager.reindexAllRepositories( null, false );

            // the above line should throw IOex
            Assert.fail( "There should be exception thrown!" );
        }
        catch ( IOException e )
        {
            // ensure we fetched from one we wanted (failingRepository)
            Assert.assertEquals( indexedProxyRepositories, fetchCountingInvocationHandler.getInvocationCount() );
            // ensure we scanned all the repositories (minus the one failed, as it failed _BEFORE_ scan invocation)
            Assert.assertEquals( indexedRepositories - 1, scanCountingInvocationHandler.getInvocationCount() );
            // ensure we have composite exception
            Assert.assertEquals( CompositeException.class, e.getCause().getClass() );
            // ensure we got back our bad exception
            Assert.assertEquals( ex, ( (CompositeException) e.getCause() ).getCauses().iterator().next() );
        }
    }

    // ==

    /**
     * {@link InvocationHandler} that simply passes the invocations to it's target.
     */
    public static class PassThruInvocationHandler
        implements InvocationHandler
    {
        private final Object delegate;

        public PassThruInvocationHandler( final Object delegate )
        {
            this.delegate = delegate;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            return method.invoke( delegate, args );
        }
    }

    /**
     * {@link InvocationHandler} that delegates the call to another {@link InvocationHandler}.
     */
    public static class DelegatingInvocationHandler
        implements InvocationHandler
    {
        private final InvocationHandler delegate;

        public DelegatingInvocationHandler( final InvocationHandler delegate )
        {
            this.delegate = delegate;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            return delegate.invoke( proxy, method, args );
        }
    }

    /**
     * {@link InvocationHandler} that counts the invocations of specified {@link Method}.
     */
    public static class CountingInvocationHandler
        extends DelegatingInvocationHandler
    {
        private final Method method;

        private int count;

        public CountingInvocationHandler( final InvocationHandler delegate, final Method countedMethod )
        {
            super( delegate );
            this.method = countedMethod;
            this.count = 0;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            if ( method.equals( this.method ) )
            {
                count++;
            }
            return super.invoke( proxy, method, args );
        }

        public int getInvocationCount()
        {
            return count;
        }
    }

    /**
     * {@link InvocationHandler} that fails if specified {@link Method} with specified arguments (as matched by
     * {@link Predicate}) is invoked. Failing is simulated with preset {@link Exception}.
     */
    public static class FailingInvocationHandler
        extends DelegatingInvocationHandler
    {
        private final Method method;

        private final Predicate<Object[]> methodArgumentsPredicate;

        private final Exception failure;

        public FailingInvocationHandler( final InvocationHandler delegate, final Method failingMethod,
                                         final Predicate<Object[]> methodArgumentsPredicate, final Exception failure )
        {
            super( delegate );
            this.method = failingMethod;
            this.methodArgumentsPredicate = methodArgumentsPredicate;
            this.failure = failure;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            if ( method.equals( this.method ) && methodArgumentsPredicate.apply( args ) )
            {
                throw failure;
            }
            else
            {
                return super.invoke( proxy, method, args );
            }
        }
    }
}
