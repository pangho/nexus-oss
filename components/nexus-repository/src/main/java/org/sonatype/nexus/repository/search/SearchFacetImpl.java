/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.search;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.entity.EntityHelper;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.StorageTxHook;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * Default {@link SearchFacet} implementation. It depends on presence of a {@link StorageFacet} attached to {@link
 * Repository}.
 *
 * @since 3.0
 */
@Named
public class SearchFacetImpl
    extends FacetSupport
    implements SearchFacet
{
  private final SearchService searchService;

  private final Map<String, ComponentMetadataProducer> componentMetadataProducers;

  private final Supplier<StorageTxHook> searchHook = new Supplier<StorageTxHook>()
  {
    @Override
    public StorageTxHook get() {
      return new SearchHook(SearchFacetImpl.this);
    }
  };

  @Inject
  public SearchFacetImpl(final SearchService searchService,
                         final Map<String, ComponentMetadataProducer> componentMetadataProducers)
  {
    this.searchService = checkNotNull(searchService);
    this.componentMetadataProducers = checkNotNull(componentMetadataProducers);
  }

  @Override
  @Guarded(by = STARTED)
  public void rebuildIndex() {
    log.info("Rebuilding index of repository {}", getRepository().getName());
    searchService.rebuildIndex(getRepository());
    UnitOfWork.begin(facet(StorageFacet.class).txSupplier());
    try {
      rebuildComponentIndex();
    }
    finally {
      UnitOfWork.end();
    }
  }

  @Transactional
  protected void rebuildComponentIndex() {
    final StorageTx tx = UnitOfWork.currentTransaction();
    for (Component component : tx.browseComponents(tx.getBucket())) {
      try {
        put(component, tx.browseAssets(component));
      }
      catch (IOException e) {
        // one "bad" should not stop processing whole repository
        log.warn("Could not reindex component: {}", component, e);
      }
    }
  }

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
    facet(StorageFacet.class).registerHookSupplier(searchHook);
  }

  /**
   * Indexes a component with given id.
   */
  @Guarded(by = STARTED)
  @Transactional
  protected void put(final EntityId componentId) {
    checkNotNull(componentId);
    try {
      Component component;
      final StorageTx tx = UnitOfWork.currentTransaction();
      component = tx.findComponent(componentId, tx.getBucket());
      if (component == null) {
        return;
      }
      put(component, tx.browseAssets(component));
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Deindexes a component with given id.
   */
  @Guarded(by = STARTED)
  protected void delete(final EntityId componentId) {
    checkNotNull(componentId);
    searchService.delete(getRepository(), componentId.toString());
  }

  @Override
  protected void doStart() throws Exception {
    searchService.createIndex(getRepository());
  }

  @Override
  protected void doDelete() {
    facet(StorageFacet.class).unregisterHookSupplier(searchHook);
    searchService.deleteIndex(getRepository());
  }

  Supplier<StorageTx> txSupplier() {
    return getRepository().facet(StorageFacet.class).txSupplier();
  }

  /**
   * Extracts metadata from passed in {@link Component} and {@link Asset}s, and PUTs it into the repository's index.
   *
   * @throws IOException if some problem happens during metadata production.
   */
  private void put(final Component component, final Iterable<Asset> assets) throws IOException {
    Map<String, Object> additional = Maps.newHashMap();
    additional.put(P_REPOSITORY_NAME, getRepository().getName());
    String json = JsonUtils.merge(componentMetadata(component, assets), JsonUtils.from(additional));
    searchService.put(getRepository(), EntityHelper.id(component).toString(), json);
  }

  /**
   * Creates component metadata to be indexed out of a component using {@link ComponentMetadataProducer} specific to
   * component {@link Format}.
   * If one is not available will use a default one ({@link DefaultComponentMetadataProducer}).
   */
  private String componentMetadata(final Component component, final Iterable<Asset> assets) {
    checkNotNull(component);
    String format = component.format();
    ComponentMetadataProducer producer = componentMetadataProducers.get(format);
    if (producer == null) {
      producer = componentMetadataProducers.get("default");
    }
    checkState(producer != null, "Could not find a component metadata producer for format: {}", format);
    return producer.getMetadata(component, assets);
  }
}
