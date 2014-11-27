/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.model.upgrade;

import org.sonatype.configuration.upgrade.ConfigurationUpgrader;
import org.sonatype.security.model.Configuration;

/**
 * A component involved only if old security configuration is found. It will fetch the old configuration, transform it
 * to current Configuration model and return it. Nothing else.
 *
 * @author cstamas
 */
public interface SecurityConfigurationUpgrader
    extends ConfigurationUpgrader<Configuration>
{

}