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
/**
 * Tests bookmarking as an admin.
 */
StartTest(function(t) {

  var ignoredFeatures = [
    '/browse/Upload', //challenges for Authenticate when logged in already
    '/browse/Search/Saved',    //challenges for Authenticate when logged in already
    '/admin/Support/Logging/Log Viewer', //errors on load?
    '/admin/Support/Support Request', //challenges for Authenticate when logged in already
    '/admin/Security/Realms' //errors on load?
  ];

  t.ok(Ext);
  t.ok(NX);
  t.waitForSessionToBeInvalidated();

  t.describe('An admin can use bookmarks to navigate the entire UI', function(t) {
    var featureStore = t.Ext().StoreManager.get('Feature');
    featureStore.filter(function(item) {
      return item.get('authenticationRequired') && ignoredFeatures.indexOf(item.get('path')) === -1;
    });

    t.chain(
        t.signIn(),
        featureStore.data.each(function(feature) {
          var path = feature.get('path');
          t.it('When logged in as admin, can navigate to ' + path, function(t) {
            t.chain(
                function(next) {
                  t.navigateTo(feature.get('bookmark'));
                  next();
                },
                {waitFor: 'bookmark', args: feature.get('bookmark')},
                {waitFor: 'Feature', args: feature.get('text')},
                function(next) {
                  t.waitForFn(function() {
                    return t.global.location.hash === '#' + feature.get('bookmark');
                  }, next)
                }
            )

          });
        })
    )
    featureStore.clearFilter(true);
  }, 300000)
});