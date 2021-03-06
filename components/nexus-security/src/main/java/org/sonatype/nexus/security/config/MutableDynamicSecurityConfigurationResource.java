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
package org.sonatype.nexus.security.config;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.common.concurrent.Locks;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mutable {@link DynamicSecurityConfigurationResource}.
 *
 * @since 3.0
 */
public class MutableDynamicSecurityConfigurationResource
    extends ComponentSupport
    implements DynamicSecurityConfigurationResource
{
  private final SecurityConfiguration model = new MemorySecurityConfiguration();

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private volatile boolean dirty = true;

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public SecurityConfiguration getConfiguration() {
    Lock lock = Locks.read(readWriteLock);
    try {
      dirty = false;
      return model;
    }
    finally {
      lock.unlock();
    }
  }

  public static interface Mutator
  {
    void apply(SecurityConfiguration model);
  }

  public void apply(final Mutator mutator) {
    checkNotNull(mutator);

    Lock lock = Locks.write(readWriteLock);
    try {
      mutator.apply(model);
      dirty = true;
    }
    finally {
      lock.unlock();
    }
  }
}
