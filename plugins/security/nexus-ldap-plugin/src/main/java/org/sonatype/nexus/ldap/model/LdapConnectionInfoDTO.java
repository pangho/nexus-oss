/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2009-11-11 15:14:14,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.ldap.model;

import javax.xml.bind.annotation.XmlType;

/**
 * LDAP Connection Information.
 *
 * @version $Revision$ $Date$
 */
@XmlType(name = "ldapConnectionInfo")
public class LdapConnectionInfoDTO
    implements java.io.Serializable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * Search Base.  Base DN for the connection.
   */
  private String searchBase;

  /**
   * System User.  The username of user with access to the LDAP
   * server.
   */
  private String systemUsername;

  /**
   * System Password.  The password for the System User.
   */
  private String systemPassword;

  /**
   * Authentication Scheme.  Method used for authentication:
   * none, simple, etc.
   */
  private String authScheme;

  /**
   * Protocol. The protocol used in the ldap URL: ldap, ldaps.
   */
  private String protocol;

  /**
   * Host.  The host name of the LDAP server.
   */
  private String host;

  /**
   * Port.  The port of the LDAP Server.
   */
  private int port = 0;

  /**
   * Backup mirror protocol. The protocol used for the backup
   * mirror URL: ldap, ldaps.
   */
  private String backupMirrorProtocol;

  /**
   * Backup mirror host.  The host name of the backup LDAP server.
   */
  private String backupMirrorHost;

  /**
   * Backup mirror port.  The port of the backup LDAP Server.
   */
  private int backupMirrorPort = 0;

  /**
   * SASL Realm.  The authentication realm.
   */
  private String realm;

  /**
   * Connection timeout.  Connection timeout in seconds.
   */
  private int connectionTimeout = 0;

  /**
   * Connection retry delay.  Connection retry delay in seconds.
   */
  private int connectionRetryDelay = 0;

  /**
   * Cache timeout.  Cache timeout in seconds. UNUSED: since switch to EhCache, this is unused value.
   */
  private int cacheTimeout = CACHE_TIMEOUT_DEFAULT;

  /**
   * Count of max incidents before blacklisting a connection.
   */
  private int maxIncidentsCount = 3;


  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Get authentication Scheme.  Method used for authentication:
   * none, simple, etc.
   *
   * @return String
   */
  public String getAuthScheme() {
    return this.authScheme;
  } //-- String getAuthScheme()

  /**
   * Get backup mirror host.  The host name of the backup LDAP
   * server.
   *
   * @return String
   */
  public String getBackupMirrorHost() {
    return this.backupMirrorHost;
  } //-- String getBackupMirrorHost()

  /**
   * Get backup mirror port.  The port of the backup LDAP Server.
   *
   * @return int
   */
  public int getBackupMirrorPort() {
    return this.backupMirrorPort;
  } //-- int getBackupMirrorPort()

  /**
   * Get backup mirror protocol. The protocol used for the backup
   * mirror URL: ldap, ldaps.
   *
   * @return String
   */
  public String getBackupMirrorProtocol() {
    return this.backupMirrorProtocol;
  } //-- String getBackupMirrorProtocol()

  /**
   * Get cache timeout.  Cache timeout in seconds.
   *
   * @return int
   */
  public int getCacheTimeout() {
    return this.cacheTimeout;
  } //-- int getCacheTimeout()

  /**
   * Get connection retry delay.  Connection retry delay in
   * seconds.
   *
   * @return int
   */
  public int getConnectionRetryDelay() {
    return this.connectionRetryDelay;
  } //-- int getConnectionRetryDelay()

  /**
   * Get connection timeout.  Connection timeout in seconds.
   *
   * @return int
   */
  public int getConnectionTimeout() {
    return this.connectionTimeout;
  } //-- int getConnectionTimeout()

  /**
   * Get host.  The host name of the LDAP server.
   *
   * @return String
   */
  public String getHost() {
    return this.host;
  } //-- String getHost()

  /**
   * Get port.  The port of the LDAP Server.
   *
   * @return int
   */
  public int getPort() {
    return this.port;
  } //-- int getPort()

  /**
   * Get protocol. The protocol used in the ldap URL: ldap,
   * ldaps.
   *
   * @return String
   */
  public String getProtocol() {
    return this.protocol;
  } //-- String getProtocol()

  /**
   * Get sASL Realm.  The authentication realm.
   *
   * @return String
   */
  public String getRealm() {
    return this.realm;
  } //-- String getRealm()

  /**
   * Get search Base.  Base DN for the connection.
   *
   * @return String
   */
  public String getSearchBase() {
    return this.searchBase;
  } //-- String getSearchBase()

  /**
   * Get system Password.  The password for the System User.
   *
   * @return String
   */
  public String getSystemPassword() {
    return this.systemPassword;
  } //-- String getSystemPassword()

  /**
   * Get system User.  The username of user with access to the
   * LDAP server.
   *
   * @return String
   */
  public String getSystemUsername() {
    return this.systemUsername;
  } //-- String getSystemUsername()

  /**
   * Set authentication Scheme.  Method used for authentication:
   * none, simple, etc.
   */
  public void setAuthScheme(String authScheme) {
    this.authScheme = authScheme;
  } //-- void setAuthScheme( String )

  /**
   * Set backup mirror host.  The host name of the backup LDAP
   * server.
   */
  public void setBackupMirrorHost(String backupMirrorHost) {
    this.backupMirrorHost = backupMirrorHost;
  } //-- void setBackupMirrorHost( String )

  /**
   * Set backup mirror port.  The port of the backup LDAP Server.
   */
  public void setBackupMirrorPort(int backupMirrorPort) {
    this.backupMirrorPort = backupMirrorPort;
  } //-- void setBackupMirrorPort( int )

  /**
   * Set backup mirror protocol. The protocol used for the backup
   * mirror URL: ldap, ldaps.
   */
  public void setBackupMirrorProtocol(String backupMirrorProtocol) {
    this.backupMirrorProtocol = backupMirrorProtocol;
  } //-- void setBackupMirrorProtocol( String )

  /**
   * Set cache timeout.  Cache timeout in seconds.
   */
  public void setCacheTimeout(int cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
  } //-- void setCacheTimeout( int )

  /**
   * Set connection retry delay.  Connection retry delay in
   * seconds.
   */
  public void setConnectionRetryDelay(int connectionRetryDelay) {
    this.connectionRetryDelay = connectionRetryDelay;
  } //-- void setConnectionRetryDelay( int )

  /**
   * Set connection timeout.  Connection timeout in seconds.
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  } //-- void setConnectionTimeout( int )

  /**
   * Set host.  The host name of the LDAP server.
   */
  public void setHost(String host) {
    this.host = host;
  } //-- void setHost( String )

  /**
   * Set port.  The port of the LDAP Server.
   */
  public void setPort(int port) {
    this.port = port;
  } //-- void setPort( int )

  /**
   * Set protocol. The protocol used in the ldap URL: ldap,
   * ldaps.
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  } //-- void setProtocol( String )

  /**
   * Set sASL Realm.  The authentication realm.
   */
  public void setRealm(String realm) {
    this.realm = realm;
  } //-- void setRealm( String )

  /**
   * Set search Base.  Base DN for the connection.
   */
  public void setSearchBase(String searchBase) {
    this.searchBase = searchBase;
  } //-- void setSearchBase( String )

  /**
   * Set system Password.  The password for the System User.
   */
  public void setSystemPassword(String systemPassword) {
    this.systemPassword = systemPassword;
  } //-- void setSystemPassword( String )

  /**
   * Set system User.  The username of user with access to the
   * LDAP server.
   */
  public void setSystemUsername(String systemUsername) {
    this.systemUsername = systemUsername;
  } //-- void setSystemUsername( String )

  public int getMaxIncidentsCount() {
    return maxIncidentsCount;
  }

  public void setMaxIncidentsCount(final int maxIncidentsCount) {
    this.maxIncidentsCount = maxIncidentsCount;
  }

  public static final int CACHE_TIMEOUT_DEFAULT = 30;

  public static final int CONNECTION_RETRY_DELAY_DEFAULT = 300;

}