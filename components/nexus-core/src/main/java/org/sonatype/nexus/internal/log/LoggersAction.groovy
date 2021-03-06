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
package org.sonatype.nexus.internal.log

import org.apache.karaf.shell.commands.Command
import org.apache.karaf.shell.commands.Option
import org.apache.karaf.shell.console.AbstractAction
import org.apache.karaf.shell.table.ShellTable
import org.sonatype.nexus.log.LogManager

import javax.inject.Inject
import javax.inject.Named

/**
 * Action to display configured loggers.
 *
 * @since 3.0
 */
@Named
@Command(name='loggers', scope = 'nexus', description = 'Display loggers')
class LoggersAction
  extends AbstractAction
{
  @Inject
  LogManager logManager

  @Option(name='-r', aliases = ['--reset'], description = 'Reset loggers')
  Boolean reset

  @Override
  protected def doExecute() {
    if (reset) {
      logManager.resetLoggers()
      return null
    }

    def table = new ShellTable()
    table.column('Name')
    table.column('Level').alignRight()

    def loggers = logManager.loggers
    loggers.keySet().sort().each { name ->
      table.addRow().addContent(name, loggers[name])
    }

    table.print System.out
    return null
  }
}
