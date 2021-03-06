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
package org.sonatype.nexus.internal.commands

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

import org.sonatype.nexus.common.app.SystemStatus

import org.apache.karaf.shell.commands.Command
import org.apache.karaf.shell.console.AbstractAction

/**
 * Display Nexus system status.
 *
 * @since 3.0
 */
@Named
@Command(name='status', scope = 'nexus', description = 'Nexus system status')
class StatusAction
  extends AbstractAction
{
  @Inject
  Provider<SystemStatus> systemStatusProvider

  @Override
  protected def doExecute() {
    def status = systemStatusProvider.get()
    println "Version: $status.version"
    println "Edition: $status.editionShort"
    println "State: $status.state"
    println "License installed: $status.licenseInstalled"
    println "License expired: $status.licenseExpired"
    println "License trial: $status.trialLicense"
    return null
  }
}
