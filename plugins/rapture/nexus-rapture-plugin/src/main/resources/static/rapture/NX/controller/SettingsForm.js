/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
 * Controls forms marked with settingsForm = true by adding save/discard/refresh functionality using form configured
 * api.
 *
 * @since 3.0
 */
Ext.define('NX.controller.SettingsForm', {
  extend: 'Ext.app.Controller',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      component: {
        'form[settingsForm=true]': {
          beforerender: me.loadForm
        },
        'form[settingsForm=true][editableCondition]': {
          afterrender: me.bindEditableCondition
        },
        'form[settingsForm=true][settingsFormSubmit=true] button[action=add]': {
          click: me.submitForm
        },
        'form[settingsForm=true][settingsFormSubmit=true] button[action=save]': {
          click: me.submitForm
        }
      }
    });
  },

  /**
   * @private
   */
  onRefresh: function () {
    var me = this,
        forms = Ext.ComponentQuery.query('form[settingsForm=true]');

    if (forms) {
      Ext.each(forms, function (form) {
        me.loadForm(form, {
          success: function (basicForm, action) {
            var title = me.getSettingsFormSuccessMessage(form, action);
            if (title) {
              NX.Messages.add({ text: title, type: 'default' });
            }
            form.fireEvent('loaded', form, action);
          }
        });
      });
    }
  },

  /**
   * @private
   * Loads the form if form's api load function is defined.
   */
  loadForm: function (form, options) {
    if (form.api && form.api.load) {
      form.load(Ext.applyIf(options || {}, {
        success: function (basicForm, action) {
          form.fireEvent('loaded', form, action);
        }
      }));
    }
  },

  /**
   * @private
   * Submits the form containing the button, if form's api submit function is defined.
   */
  submitForm: function (button) {
    var me = this,
        form = button.up('form');

    if (form.api && form.api.submit) {
      form.getForm().submit({
        success: function (basicForm, action) {
          var title = me.getSettingsFormSuccessMessage(form, action);
          if (title) {
            NX.Messages.add({ text: title, type: 'success' });
          }
          form.fireEvent('submitted', form, action);
          me.loadForm(form);
        }
      });
    }
  },

  /**
   * @private
   * Calculates title based on form's {NX.view.SettingsForm#getSettingsFormSuccessMessage}.
   * @param {NX.view.SettingsForm} form
   * @param {Ext.form.action.Action} action
   */
  getSettingsFormSuccessMessage: function (form, action) {
    var title;

    if (form.settingsFormSuccessMessage) {
      if (Ext.isFunction(form.settingsFormSuccessMessage)) {
        title = form.settingsFormSuccessMessage(action.result.data);
      }
      else {
        title = form.settingsFormSuccessMessage.toString();
      }
      title = title.replace(/\$action/, action.type.indexOf('submit') > -1 ? 'updated' : 'refreshed');
    }
    return title;
  },

  /**
   * @private
   * Toggle editable on settings form hen editable condition is satisfied (if specified).
   * @param {NX.view.SettingsForm} form
   */
  bindEditableCondition: function (form) {
    if (Ext.isDefined(form.editableCondition)) {
      form.mon(
          form.editableCondition,
          {
            satisfied: function () {
              form.setEditable(true);
            },
            unsatisfied: function () {
              form.setEditable(false);
            },
            scope: form
          }
      );
    }
  }

});