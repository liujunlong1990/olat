/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.user.administration.bulkchange;

import java.util.HashMap;
import java.util.HashSet;

import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.rules.RulesFactory;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.user.administration.SystemRolesAndRightsController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * first step: select attributes, which should be changed
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author rhaag
 */
class UserBulkChangeStep01 extends BasicStep {

    boolean canCreateOLATPassword;
    static final String usageIdentifyer = UserBulkChangeStep01.class.getCanonicalName();
    TextElement textAreaElement;

    public UserBulkChangeStep01(final UserRequest ureq) {
        super(ureq);
        setI18nTitleAndDescr("step1.description", null);
        setNextStep(new UserBulkChangeStep02(ureq));
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return new PrevNextFinishConfig(true, true, false);
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new UserBulkChangeStepForm01(ureq, windowControl, form, stepsRunContext);
        return stepI;
    }

    private final class UserBulkChangeStepForm01 extends StepFormBasicController {

        private FormLayoutContainer textContainer;
        private HashSet<FormItem> targets;
        private MultipleSelectionElement chkAuthor;
        private SingleSelection setAuthor;
        private MultipleSelectionElement chkUserManager;
        private SingleSelection setUserManager;
        private MultipleSelectionElement chkGroupManager;
        private SingleSelection setGroupManager;

        private MultipleSelectionElement chkAdmin;
        private SingleSelection setAdmin;
        private MultipleSelectionElement chkStatus;
        private SingleSelection setStatus;

        public UserBulkChangeStepForm01(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext) {
            super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
            // use custom translator with fallback to user properties translator
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
            flc.setTranslator(getTranslator());
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // nothing to dispose
        }

        @Override
        protected void formOK(final UserRequest ureq) {

            Boolean validChange = (Boolean) getFromRunContext("validChange");
            final HashMap<String, String> roleChangeMap = new HashMap<String, String>();

            if (chkUserManager != null && chkUserManager.getSelectedKeys().contains("Usermanager")) {
                roleChangeMap.put(Constants.GROUP_USERMANAGERS, setUserManager.getSelectedKey());
                validChange = true;
            }

            if (chkGroupManager != null && chkGroupManager.getSelectedKeys().contains("Groupmanager")) {
                roleChangeMap.put(Constants.GROUP_GROUPMANAGERS, setGroupManager.getSelectedKey());
                validChange = true;
            }

            if (chkAuthor != null && chkAuthor.getSelectedKeys().contains("Author")) {
                roleChangeMap.put(Constants.GROUP_AUTHORS, setAuthor.getSelectedKey());
                validChange = true;
            }

            if (chkAdmin != null && chkAdmin.getSelectedKeys().contains("Admin")) {
                roleChangeMap.put(Constants.GROUP_ADMIN, setAdmin.getSelectedKey());
                validChange = true;
            }

            if (chkStatus != null && chkStatus.getSelectedKeys().contains("Status")) {
                roleChangeMap.put("Status", setStatus.getSelectedKey());
                validChange = true;
            }

            addToRunContext("roleChangeMap", roleChangeMap);
            addToRunContext("validChange", validChange);

            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
            // always true, because no changes are required
            return true;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            setFormTitle("step1.title");

            textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step1.html");
            formLayout.add(textContainer);
            final String[] addremove = new String[] { "add", "remove" };
            final String[] addremoveTranslated = new String[] { translate("role.add"), translate("role.remove") };

            // Main layout is a vertical layout without left side padding. To format
            // the checkboxes properly we need a default layout for the remaining form
            // elements
            final FormItemContainer innerFormLayout = FormLayoutContainer.createDefaultFormLayout("innerFormLayout", getTranslator());
            formLayout.add(innerFormLayout);

            Roles roles = getBaseSecurityEBL().getRoles(ureq.getIdentity());

            // usermanager:
            if (roles.isOLATAdmin() || roles.isUserManager()) {
                chkUserManager = uifactory.addCheckboxesVertical("Usermanager", "table.role.useradmin", innerFormLayout, new String[] { "Usermanager" },
                        new String[] { "" }, null, 1);
                chkUserManager.select("Usermanager", false);
                chkUserManager.addActionListener(listener, FormEvent.ONCLICK);

                setUserManager = uifactory.addDropdownSingleselect("setUserManager", null, innerFormLayout, addremove, addremoveTranslated, null);
                setUserManager.setVisible(false);
                targets = new HashSet<FormItem>();
                targets.add(setUserManager);
                RulesFactory.createHideRule(chkUserManager, null, targets, innerFormLayout);
                RulesFactory.createShowRule(chkUserManager, "Usermanager", targets, innerFormLayout);
            }

            // groupmanager
            if (roles.isOLATAdmin() || roles.isGroupManager()) {
                chkGroupManager = uifactory.addCheckboxesVertical("Groupmanager", "table.role.groupadmin", innerFormLayout, new String[] { "Groupmanager" },
                        new String[] { "" }, null, 1);
                chkGroupManager.select("Groupmanager", false);
                chkGroupManager.addActionListener(listener, FormEvent.ONCLICK);

                setGroupManager = uifactory.addDropdownSingleselect("setGroupManager", null, innerFormLayout, addremove, addremoveTranslated, null);
                setGroupManager.setVisible(false);
                targets = new HashSet<FormItem>();
                targets.add(setGroupManager);
                RulesFactory.createHideRule(chkGroupManager, null, targets, innerFormLayout);
                RulesFactory.createShowRule(chkGroupManager, "Groupmanager", targets, innerFormLayout);
            }

            // author
            if (roles.isOLATAdmin() || roles.isAuthor()) {
                chkAuthor = uifactory.addCheckboxesVertical("Author", "table.role.author", innerFormLayout, new String[] { "Author" }, new String[] { "" }, null, 1);
                chkAuthor.select("Author", false);
                chkAuthor.addActionListener(listener, FormEvent.ONCLICK);

                setAuthor = uifactory.addDropdownSingleselect("setAuthor", null, innerFormLayout, addremove, addremoveTranslated, null);
                setAuthor.setVisible(false);
                targets = new HashSet<FormItem>();
                targets.add(setAuthor);
                RulesFactory.createHideRule(chkAuthor, null, targets, innerFormLayout);
                RulesFactory.createShowRule(chkAuthor, "Author", targets, innerFormLayout);
            }

            // sysadmin
            if (roles.isOLATAdmin()) {
                chkAdmin = uifactory.addCheckboxesVertical("Admin", "table.role.admin", innerFormLayout, new String[] { "Admin" }, new String[] { "" }, null, 1);
                chkAdmin.select("Admin", false);
                chkAdmin.addActionListener(listener, FormEvent.ONCLICK);

                setAdmin = uifactory.addDropdownSingleselect("setAdmin", null, innerFormLayout, addremove, addremoveTranslated, null);
                setAdmin.setVisible(false);
                targets = new HashSet<FormItem>();
                targets.add(setAdmin);
                RulesFactory.createHideRule(chkAdmin, null, targets, innerFormLayout);
                RulesFactory.createShowRule(chkAdmin, "Admin", targets, innerFormLayout);
            }

            // status
            if (roles.isOLATAdmin()) {
                chkStatus = uifactory.addCheckboxesVertical("Status", "table.role.status", innerFormLayout, new String[] { "Status" }, new String[] { "" }, null, 1);
                chkStatus.select("Status", false);
                chkStatus.addActionListener(listener, FormEvent.ONCLICK);

                // TODO: RH: pay attention: if status changes in Identity-statics this
                // may lead to missing status
                // implement methods in SystemRolesAndRightsController.java
                setTranslator(PackageUtil.createPackageTranslator(SystemRolesAndRightsController.class, getLocale()));
                final String[] statusKeys = { Integer.toString(Identity.STATUS_ACTIV), Integer.toString(Identity.STATUS_PERMANENT),
                        Integer.toString(Identity.STATUS_LOGIN_DENIED) };
                final String[] statusValues = { translate("rightsForm.status.activ"), translate("rightsForm.status.permanent"),
                        translate("rightsForm.status.login_denied") };

                setStatus = uifactory.addDropdownSingleselect("setStatus", null, innerFormLayout, statusKeys, statusValues, null);
                setStatus.setVisible(false);
                targets = new HashSet<FormItem>();
                targets.add(setStatus);
                RulesFactory.createHideRule(chkStatus, null, targets, innerFormLayout);
                RulesFactory.createShowRule(chkStatus, "Status", targets, innerFormLayout);
            }

        }

        /**
         * @return
         */
        private BaseSecurityEBL getBaseSecurityEBL() {
            return CoreSpringFactory.getBean(BaseSecurityEBL.class);
        }

        private UserService getUserService() {
            return CoreSpringFactory.getBean(UserService.class);
        }

    }

}
