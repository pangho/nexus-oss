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
/*global Ext, NX*/

/**
 * Repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Repositories', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: [
    'nx-coreui-repository-list'
  ],
  models: [
    'Repository'
  ],
  stores: [
    'Blobstore',
    'Repository',
    'RepositoryRecipe',
    'RepositoryReference'
  ],
  views: [
    'repository.RepositoryAdd',
    'repository.RepositoryFeature',
    'repository.RepositoryList',
    'repository.RepositorySelectRecipe',
    'repository.RepositorySettings',
    'repository.RepositorySettingsForm',
    'repository.recipe.Maven2Hosted',
    'repository.recipe.Maven2Proxy',
    'repository.recipe.Maven2Group',
    'repository.recipe.NugetHosted',
    'repository.recipe.NugetProxy',
    'repository.recipe.NugetGroup',
    'repository.recipe.RawHosted',
    'repository.recipe.RawProxy',
    'repository.recipe.RawGroup'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'list', selector: 'nx-coreui-repository-list' },
    { ref: 'settings', selector: 'nx-coreui-repository-feature nx-coreui-repository-settings' }
  ],
  icons: {
    'repository-hosted': {
      file: 'database.png',
      variants: ['x16', 'x32']
    },
    'repository-proxy': {
      file: 'database_link.png',
      variants: ['x16', 'x32']
    },
    'repository-group': {
      file: 'folder_database.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Repository/Repositories',
    text: NX.I18n.get('Repositories_Text'),
    description: NX.I18n.get('Repositories_Description'),
    view: { xtype: 'nx-coreui-repository-feature' },
    iconConfig: {
      file: 'database.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      // Show feature if the current user is permitted any repository-admin permissions
      return NX.Permissions.checkExistsWithPrefix('nexus:repository-admin');
    }
  },

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadStores
        },
        '#State': {
          receivingchanged: me.onStateReceivingChanged
        }
      },
      store: {
        '#Repository': {
          load: me.reselect
        }
      },
      component: {
        'nx-coreui-repository-list': {
          beforerender: me.loadStores,
          afterrender: me.startStatusPolling,
          beforedestroy: me.stopStatusPolling
        },
        'nx-coreui-repository-list button[action=new]': {
          click: me.showSelectRecipePanel
        },
        'nx-coreui-repository-feature button[action=rebuildIndex]': {
          click: me.rebuildIndex,
          afterrender: me.bindIfProxyOrHostedAndEditable
        },
        'nx-coreui-repository-feature button[action=invalidateProxyCache]': {
          click: me.invalidateProxyCache,
          afterrender: me.bindIfProxyAndEditable
        },
        'nx-coreui-repository-feature button[action=invalidateNegativeCache]': {
          click: me.invalidateNegativeCache,
          afterrender: me.bindIfProxyAndEditable
        },
        'nx-coreui-repository-settings-form': {
          submitted: me.loadStores
        },
        'nx-coreui-repository-selectrecipe': {
          cellclick: me.showAddRepositoryPanel
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function(model) {
    return model.get('name');
  },

  /**
   * @override
   */
  onSelection: function(list, model) {
    var me = this,
        settingsPanel = me.getSettings(),
        formCls = Ext.ClassManager.getByAlias('widget.nx-coreui-repository-' + model.get('recipe'));

    Ext.suspendLayouts();

    if (!formCls) {
      me.logWarn('Could not find settings form for: ' + model.getId());
    }
    else {
      if (Ext.isDefined(model)) {
        // Load the form
        settingsPanel.removeAllSettingsForms();
        settingsPanel.addSettingsForm({ xtype: formCls.xtype, recipe: model });
        settingsPanel.loadRecord(model);

        // Set immutable fields to readonly
        Ext.Array.each(settingsPanel.query('field[readOnlyOnUpdate=true]'), function(field) {
          field.setReadOnly(true);
          field.addCls('nx-combo-disabled');
        });
      }
    }
    Ext.resumeLayouts();
  },

  /**
   * @private
   */
  showSelectRecipePanel: function() {
    var me = this;

    // Show the first panel in the create wizard, and set the breadcrumb
    me.setItemName(1, NX.I18n.get('Repositories_SelectRecipe_Title'));
    me.setItemClass(1, NX.Icons.cls('repository-hosted', 'x16'));

    // Show the panel
    me.loadCreateWizard(1, true, Ext.widget({
      xtype: 'panel',
      layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
      },
      items: [
        { xtype: 'nx-actions' },
        {
          xtype: 'nx-coreui-repository-selectrecipe',
          flex: 1
        }
      ]
    }));
  },

  /**
   * @private
   */
  showAddRepositoryPanel: function(list, td, cellIndex, model) {
    var me = this,
        formCls = Ext.ClassManager.getByAlias('widget.nx-coreui-repository-' + model.getId());

    if (!formCls) {
      me.logWarn('Could not find settings form for: ' + model.getId());
    }
    else {
      // Show the second panel in the create wizard, and set the breadcrumb
      me.setItemName(2, NX.I18n.format('Repositories_Create_Title', model.get('name')));
      me.setItemClass(2, NX.Icons.cls('repository-hosted', 'x16'));
      me.loadCreateWizard(2, true, { xtype: 'nx-coreui-repository-add', recipe: model });
    }
  },

  /**
   * @private
   */
  deleteModel: function(model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_Repository.remove(model.getId(), function(response) {
      me.getStore('Repository').load();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository deleted: ' + description, type: 'success' });
      }
    });
  },

  /**
   * @private
   * Start polling for repository statuses.
   */
  startStatusPolling: function() {
    var me = this;

    if (me.statusProvider) {
      me.statusProvider.disconnect();
    }
    me.statusProvider = Ext.direct.Manager.addProvider({
      type: 'polling',
      url: NX.direct.api.POLLING_URLS.coreui_Repository_readStatus,
      interval: 5000,
      baseParams: {
      },
      listeners: {
        data: function(provider, event) {
          if (event.data && event.data.success && event.data.data) {
            me.updateRepositoryModels(event.data.data);
          }
        },
        scope: me
      }
    });

    //<if debug>
    me.logDebug('Repository status pooling started');
    //</if>
  },

  /**
   * @private
   * Stop polling for repository statuses.
   */
  stopStatusPolling: function() {
    var me = this;

    if (me.statusProvider) {
      me.statusProvider.disconnect();
    }

    //<if debug>
    me.logDebug('Repository status pooling stopped');
    //</if>
  },

  /**
   * @private
   * Updates Repository store records with values returned by status polling.
   * @param {Array} repositoryStatuses array of status objects
   */
  updateRepositoryModels: function(repositoryStatuses) {
    var me = this;

    Ext.Array.each(repositoryStatuses, function(repositoryStatus) {
      var repositoryModel = me.getStore('Repository').findRecord('name', repositoryStatus.repositoryName);
      if (repositoryModel) {
        repositoryModel.set('status', repositoryStatus);
        repositoryModel.commit(true);
      }
    });
  },

  /**
   * Start / Stop status pooling when server is disconnected/connected.
   * @param receiving if we are receiving or not status from server (server connected/disconnected)
   */
  onStateReceivingChanged: function(receiving) {
    var me = this;

    if (me.getList() && receiving) {
      me.startStatusPolling();
    }
    else {
      me.stopStatusPolling();
    }
  },

  /**
   * @private
   * Rebuild repository index for the selected Repository.
   */
  rebuildIndex: function() {
    var me = this,
        model = me.getList().getSelectionModel().getLastSelected();

    NX.direct.coreui_Repository.rebuildIndex(model.getId(), function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository index rebuilt: ' + me.getDescription(model), type: 'success' });
      }
    });
  },

  /**
   * @private
   * Invalidate proxy cache for the selected proxy Repository.
   */
  invalidateProxyCache: function() {
    var me = this,
        model = me.getList().getSelectionModel().getLastSelected();

    NX.direct.coreui_Repository.invalidateProxyCache(model.getId(), function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository proxy cache invalidated: ' + me.getDescription(model), type: 'success' });
      }
    });
  },

  /**
   * @private
   * Invalidate negative cache for the selected proxy Repository.
   */
  invalidateNegativeCache: function() {
    var me = this,
        model = me.getList().getSelectionModel().getLastSelected();

    NX.direct.coreui_Repository.invalidateNegativeCache(model.getId(), function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository negative cache invalidated: ' + me.getDescription(model), type: 'success' });
      }
    });
  },

  /**
   * @private
   * Enables button if the select repository is a proxy or hosted repository.
   */
  bindIfProxyOrHostedAndEditable: function (button) {
    var permittedCondition;
    button.mon(
        NX.Conditions.and(
            permittedCondition = NX.Conditions.isPermitted('nexus:repository-admin:*:*:edit'),
            NX.Conditions.gridHasSelection('nx-coreui-repository-list', function(model) {
              permittedCondition.setPermission(
                  'nexus:repository-admin:' + model.get('format') + ':' + model.get('name') + ':edit'
              );
              return true;
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
    button.mon(
        NX.Conditions.gridHasSelection('nx-coreui-repository-list', function(model) {
          return model.get('type') === 'proxy' || model.get('type') === 'hosted';
        }),
        {
          satisfied: button.show,
          unsatisfied: button.hide,
          scope: button
        }
    );
  },

  /**
   * @private
   * Enables button if the select repository is a proxy repository.
   */
  bindIfProxyAndEditable: function (button) {
    var permittedCondition;
    button.mon(
        NX.Conditions.and(
            permittedCondition = NX.Conditions.isPermitted('nexus:repository-admin:*:*:edit'),
            NX.Conditions.gridHasSelection('nx-coreui-repository-list', function(model) {
              permittedCondition.setPermission(
                  'nexus:repository-admin:' + model.get('format') + ':' + model.get('name') + ':edit'
              );
              return true;
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
    button.mon(
        NX.Conditions.gridHasSelection('nx-coreui-repository-list', function(model) {
          return model.get('type') === 'proxy';
        }),
        {
          satisfied: button.show,
          unsatisfied: button.hide,
          scope: button
        }
    );
  },

  /**
   * @override
   * @protected
   * Enable 'New' when user has 'add' permission.
   */
  bindNewButton: function(button) {
    button.mon(
        NX.Conditions.isPermitted('nexus:repository-admin:*:*:add'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @protected
   * Enable 'Delete' when user has 'delete' permission for selected repository.
   */
  bindDeleteButton: function(button) {
    var permittedCondition;
    button.mon(
        NX.Conditions.and(
            permittedCondition = NX.Conditions.isPermitted('nexus:repository-admin:*:*:delete'),
            NX.Conditions.gridHasSelection('nx-coreui-repository-list', function(model) {
              permittedCondition.setPermission(
                  'nexus:repository-admin:' + model.get('format') + ':' + model.get('name') + ':delete'
              );
              return true;
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});
