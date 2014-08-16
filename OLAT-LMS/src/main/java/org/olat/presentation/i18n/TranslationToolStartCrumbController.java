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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.lms.commons.i18n.I18nItem;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.preferences.Preferences;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.progressbar.ProgressBar;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.breadcrumb.BreadCrumbController;
import org.olat.presentation.framework.core.control.generic.breadcrumb.CrumbFormBasicController;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> This is the start controller for the translation tool workflow. It offers a panel where translators can start their translation workflow using
 * after different strategies: translate missing, translate all or search explicitly <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 09.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

class TranslationToolStartCrumbController extends CrumbFormBasicController {
    private static final String KEYS_ENABLED = "enabled";
    private static final String KEYS_EMPTY = "";
    private static final String KEYS_VALUE = "value";
    private static final String KEYS_KEY = "key";
    private static final String KEYS_REFERENCE = "reference";
    private static final String KEYS_TARGET = "target";
    private static final String ALL_BUNDLES_IDENTIFYER = "all";
    // main reference and language selection elements
    private SingleSelection referenceLangSelection;
    private SingleSelection targetLangSelection;
    private ProgressBar progressBar;
    // missing items form elements
    private FormLink missingListButton;
    private FormLink missingTranslateButton;
    private SingleSelection missingBundlesSelection;
    private MultipleSelectionElement missingBundlesIncludeBundlesChildrenSwitch;
    private MultipleSelectionElement missingBundlesPrioritySortSwitch;
    // existing items form elements
    private FormLink existingListButton;
    private FormLink existingTranslateButton;
    private SingleSelection existingBundlesSelection;
    private MultipleSelectionElement existingBundlesIncludeBundlesChildrenSwitch;
    private MultipleSelectionElement existingBundlesPrioritySortSwitch;
    // all items form elements
    private FormLink allListButton;
    private FormLink allTranslateButton;
    private SingleSelection allBundlesSelection;
    private MultipleSelectionElement allBundlesIncludeBundlesChildrenSwitch;
    private MultipleSelectionElement allBundlesPrioritySortSwitch;
    // search items form elements
    private FormLink searchListButton;
    private FormLink searchTranslateButton;
    private SingleSelection searchBundlesSelection;
    private MultipleSelectionElement searchBundlesIncludeBundlesChildrenSwitch;
    private MultipleSelectionElement searchBundlesPrioritySortSwitch;
    private SingleSelection searchKeyValueSelection;
    private SingleSelection searchReferenceTargetSelection;
    private TextElement searchInput;

    // true when child controllers did save a i18n item but this view is not
    // activated
    private boolean viewIsDirty = false;
    private Locale referenceLocale;
    private Locale targetLocale;
    // true when the overlay files are edited and not the language files itself
    private boolean customizingMode = false;

    /**
     * Constructor for the start crumb controller
     * 
     * @param ureq
     * @param control
     */
    public TranslationToolStartCrumbController(UserRequest ureq, WindowControl control, boolean customizingMode) {
        super(ureq, control, "translationToolStartCrumb");
        this.customizingMode = customizingMode;
        initForm(ureq);
    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormUIFactory formFactory = FormUIFactory.getInstance();
        I18nManager i18nMgr = I18nManager.getInstance();
        List<String> bundleNames = I18nModule.getBundleNamesContainingI18nFiles();
        String[] bundlesKeys = buildBundleArrayKeys(bundleNames, true);
        String[] bundlesValues = buildBundleArrayValues(bundleNames, true);
        // call init methods for each form part
        initLanguageSelectorElements(ureq.getUserSession(), formFactory, i18nMgr, listener, formLayout);
        initMissingItemsElements(formFactory, i18nMgr, listener, formLayout, bundlesKeys, bundlesValues);
        initExistingItemsElements(formFactory, i18nMgr, listener, formLayout, bundlesKeys, bundlesValues);
        initAllItemsElements(formFactory, i18nMgr, listener, formLayout, bundlesKeys, bundlesValues);
        initSearchItemsElements(formFactory, i18nMgr, listener, formLayout, bundlesKeys, bundlesValues);
        // Initialize progress bar and statistics
        updateStatistics();
        // Override text labels for customizing mode
        if (customizingMode) {
            setCustomizingTextLabels();
        }
        this.flc.contextPut("customizingMode", Boolean.valueOf(customizingMode));
        this.flc.contextPut("customizingPrefix", (customizingMode ? "customize." : ""));
    }

    private void initLanguageSelectorElements(UserSession usess, FormUIFactory formFactory, I18nManager i18nMgr, Controller listener, FormItemContainer formLayout) {
        // Add language selection as a subform
        List<String> referenceLangs;
        if (customizingMode) {
            // Add all enabled languages that can be customized
            referenceLangs = new ArrayList<String>(I18nModule.getEnabledLanguageKeys());
        } else {
            // Add configured reference languages in translation mode
            referenceLangs = I18nModule.getTransToolReferenceLanguages();
        }
        String[] referencelangKeys = ArrayHelper.toArray(referenceLangs);
        String[] referenceLangValues = new String[referencelangKeys.length];
        for (int i = 0; i < referencelangKeys.length; i++) {
            String key = referencelangKeys[i];
            String explLang = i18nMgr.getLanguageInEnglish(key, false);
            String all = explLang;
            if (explLang != null && !explLang.equals(key))
                all += " (" + key + ")";
            referenceLangValues[i] = all;
        }
        ArrayHelper.sort(referencelangKeys, referenceLangValues, false, true, false);
        // Build css classes for reference languages
        String[] referenceLangCssClasses = i18nMgr.createLanguageFlagsCssClasses(referencelangKeys, "b_with_small_icon_left");
        // Preset first of the reference locales
        Preferences guiPrefs = usess.getGuiPreferences();
        String referencePrefs = (String) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_REFERENCE_LANG, referenceLangs.get(0));
        referenceLocale = i18nMgr.getLocaleOrNull(referencePrefs);
        referenceLangSelection = formFactory.addDropdownSingleselect("start.referenceLangSelection", formLayout, referencelangKeys, referenceLangValues,
                referenceLangCssClasses);
        referenceLangSelection.select(referenceLocale.toString(), true);
        this.flc.contextPut("referenceLangKey", referenceLocale.toString());
        referenceLangSelection.addActionListener(this, FormEvent.ONCHANGE);
        // Add target languages without overlays
        Set<String> translatableKeys;
        if (customizingMode) {
            // Use all enabled languages in customizing mode
            translatableKeys = I18nModule.getOverlayLanguageKeys();
        } else {
            // Since 6.2 the user can only translate the language he has set in his preferences
            translatableKeys = new HashSet<String>();
            if (I18nModule.getTranslatableLanguageKeys().contains(i18nMgr.getLocaleKey(getLocale()))) {
                translatableKeys.add(i18nMgr.getLocaleKey(getLocale()));
            } else {
                // ups, user language is not translatable, give user choice to
                // select one of the translatable languages
                translatableKeys = I18nModule.getTranslatableLanguageKeys();
            }
        }
        String[] targetlangKeys = ArrayHelper.toArray(translatableKeys);
        String[] targetLangValues = new String[targetlangKeys.length];
        for (int i = 0; i < targetlangKeys.length; i++) {
            String key = targetlangKeys[i];
            // Since in case of customizing mode the target lang key does already
            // contain the customizing key, there is no need to get the language with
            // overlay enabled, this would double the customizing extension to the key
            String explLang;
            if (customizingMode) {
                String origKey = i18nMgr.createOrigianlLocaleKeyForOverlay(I18nModule.getAllLocales().get(key));
                explLang = i18nMgr.getLanguageInEnglish(origKey, true);
            } else {
                explLang = i18nMgr.getLanguageInEnglish(key, false);
            }
            String all = explLang;
            if (explLang != null && !explLang.equals(key))
                all += "   (" + key + ")";
            targetLangValues[i] = all;
        }
        ArrayHelper.sort(targetlangKeys, targetLangValues, false, true, false);
        // Build css classes for reference languages
        String[] targetLangCssClasses = i18nMgr.createLanguageFlagsCssClasses(targetlangKeys, "b_with_small_icon_left");
        targetLangSelection = formFactory.addDropdownSingleselect("start.targetLangSelection", formLayout, targetlangKeys, targetLangValues, targetLangCssClasses);
        // Select current language if in list or the first one in the menu
        if (customizingMode) {
            // Use same as reference language in customizing mode
            targetLocale = I18nModule.getOverlayLocales().get(referenceLocale);
            // Disable target lang selection - user should only choose reference language, target is updated automatically
            targetLangSelection.setEnabled(false);
        } else {
            // Use users current language in translation mode
            targetLocale = getTranslator().getLocale();
            if (!Arrays.asList(targetlangKeys).contains(i18nMgr.getLocaleKey(targetLocale))) {
                targetLocale = i18nMgr.getLocaleOrNull(targetlangKeys[0]);
            }
        }
        targetLangSelection.select(i18nMgr.getLocaleKey(targetLocale), true);
        // Add lang key for image - don't use customizing lang key
        if (customizingMode) {
            this.flc.contextPut("targetLangKey", i18nMgr.createOrigianlLocaleKeyForOverlay(targetLocale));
        } else {
            this.flc.contextPut("targetLangKey", targetLocale.toString());
        }
        targetLangSelection.addActionListener(this, FormEvent.ONCHANGE);
        // Add progress bar as normal component (not a form element)
        int bundlesCount = i18nMgr.countBundles(null, true);
        progressBar = new ProgressBar("progressBar", 200, 0, 0, translate("start.progressBar.unitLabel", bundlesCount + ""));
        this.flc.put("progressBar", progressBar);
    }

    private void initMissingItemsElements(FormUIFactory formFactory, I18nManager i18nMgr, Controller listener, FormItemContainer formLayout, String[] bundlesKeys,
            String[] bundlesValues) {
        // Add missing bundles selector
        missingBundlesSelection = formFactory.addDropdownSingleselect("missingBundlesSelection", this.flc, bundlesKeys, bundlesValues, null);
        missingBundlesSelection.addActionListener(this, FormEvent.ONCHANGE);
        missingBundlesSelection.select(ALL_BUNDLES_IDENTIFYER, true);
        // Add missing bundles children switch
        missingBundlesIncludeBundlesChildrenSwitch = formFactory.addCheckboxesHorizontal("missingBundlesIncludeBundlesChildrenSwitch", null, this.flc,
                new String[] { KEYS_ENABLED }, new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        missingBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
        missingBundlesIncludeBundlesChildrenSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(missingBundlesIncludeBundlesChildrenSwitch);
        missingBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
        // Add priority sort switch
        missingBundlesPrioritySortSwitch = formFactory.addCheckboxesHorizontal("missingBundlesPrioritySortSwitch", null, this.flc, new String[] { KEYS_ENABLED },
                new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        missingBundlesPrioritySortSwitch.select(KEYS_ENABLED, true);
        missingBundlesPrioritySortSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(missingBundlesPrioritySortSwitch);
        // Add button to trigger missing keys search
        missingListButton = new FormLinkImpl("missingListButton", "missingListButton", "generic.listButton", Link.BUTTON);
        formLayout.add(missingListButton);
        missingTranslateButton = new FormLinkImpl("missingTranslateButton", "missingTranslateButton", "generic.translateButton", Link.BUTTON);
        formLayout.add(missingTranslateButton);
    }

    private void initExistingItemsElements(FormUIFactory formFactory, I18nManager i18nMgr, Controller listener, FormItemContainer formLayout, String[] bundlesKeys,
            String[] bundlesValues) {
        // Add existing bundles selector
        existingBundlesSelection = formFactory.addDropdownSingleselect("existingBundlesSelection", this.flc, bundlesKeys, bundlesValues, null);
        existingBundlesSelection.addActionListener(this, FormEvent.ONCHANGE);
        existingBundlesSelection.select(ALL_BUNDLES_IDENTIFYER, true);
        // Add existing bundles children switch
        existingBundlesIncludeBundlesChildrenSwitch = formFactory.addCheckboxesHorizontal("existingBundlesIncludeBundlesChildrenSwitch", null, this.flc,
                new String[] { KEYS_ENABLED }, new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        existingBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
        existingBundlesIncludeBundlesChildrenSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(existingBundlesIncludeBundlesChildrenSwitch);
        existingBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
        // Add priority sort switch
        existingBundlesPrioritySortSwitch = formFactory.addCheckboxesHorizontal("existingBundlesPrioritySortSwitch", null, this.flc, new String[] { KEYS_ENABLED },
                new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        existingBundlesPrioritySortSwitch.select(KEYS_ENABLED, true);
        existingBundlesPrioritySortSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(existingBundlesPrioritySortSwitch);
        // Add button to trigger existing keys search
        existingListButton = new FormLinkImpl("existingListButton", "existingListButton", "generic.listButton", Link.BUTTON);
        formLayout.add(existingListButton);
        existingTranslateButton = new FormLinkImpl("existingTranslateButton", "existingTranslateButton", "generic.translateButton", Link.BUTTON);
        formLayout.add(existingTranslateButton);
    }

    private void initAllItemsElements(FormUIFactory formFactory, I18nManager i18nMgr, Controller listener, FormItemContainer formLayout, String[] bundlesKeys,
            String[] bundlesValues) {
        // Add all bundles selector
        allBundlesSelection = formFactory.addDropdownSingleselect("allBundlesSelection", this.flc, bundlesKeys, bundlesValues, null);
        allBundlesSelection.addActionListener(this, FormEvent.ONCHANGE);
        allBundlesSelection.select(ALL_BUNDLES_IDENTIFYER, true);
        // Add all bundles children switch
        allBundlesIncludeBundlesChildrenSwitch = formFactory.addCheckboxesHorizontal("allBundlesIncludeBundlesChildrenSwitch", null, this.flc,
                new String[] { KEYS_ENABLED }, new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        allBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
        allBundlesIncludeBundlesChildrenSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(allBundlesIncludeBundlesChildrenSwitch);
        allBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
        // Add priority sort switch
        allBundlesPrioritySortSwitch = formFactory.addCheckboxesHorizontal("allBundlesPrioritySortSwitch", null, this.flc, new String[] { KEYS_ENABLED },
                new String[] { KEYS_EMPTY }, null);// disabled label by setting i18nLabel to null
        allBundlesPrioritySortSwitch.select(KEYS_ENABLED, true);
        allBundlesPrioritySortSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(allBundlesPrioritySortSwitch);
        // Add button to trigger all keys search
        allListButton = new FormLinkImpl("allListButton", "allListButton", "generic.listButton", Link.BUTTON);
        formLayout.add(allListButton);
        allTranslateButton = new FormLinkImpl("allTranslateButton", "allTranslateButton", "generic.translateButton", Link.BUTTON);
        formLayout.add(allTranslateButton);
    }

    private void initSearchItemsElements(FormUIFactory formFactory, I18nManager i18nMgr, Controller listener, FormItemContainer formLayout, String[] bundlesKeys,
            String[] bundlesValues) {
        FormItemContainer searchLayoutContainer = (FormItemContainer) FormLayoutContainer.createDefaultFormLayout("searchLayoutContainer", getTranslator());
        formLayout.add(searchLayoutContainer);
        // Add search input field
        searchInput = formFactory.addTextElement("searchInput", "start.search.input", 100, "", searchLayoutContainer);
        // Add search selecton for searching in keys or values
        String[] searchKeyValueKeys = new String[] { KEYS_KEY, KEYS_VALUE };
        String[] searchKeyValueValues = new String[] { translate("generic.key"), translate("generic.value") };
        searchKeyValueSelection = formFactory.addRadiosHorizontal("searchKeyValueSelection", "start.search.in", searchLayoutContainer, searchKeyValueKeys,
                searchKeyValueValues);
        searchKeyValueSelection.select(KEYS_VALUE, true);
        // Add search selecton for reference or target search
        String[] searchReferenceTargetKeys = new String[] { KEYS_REFERENCE, KEYS_TARGET };
        String[] searchReferenceTargetValues;
        if (customizingMode) {
            searchReferenceTargetValues = new String[] { translate("generic.customize.lang.reference"), translate("generic.customize.lang.target") };
        } else {
            searchReferenceTargetValues = new String[] { translate("generic.lang.reference"), translate("generic.lang.target") };
        }
        searchReferenceTargetSelection = formFactory.addRadiosHorizontal("searchReferenceTargetSelection", "start.search.in", searchLayoutContainer,
                searchReferenceTargetKeys, searchReferenceTargetValues);
        searchReferenceTargetSelection.select(KEYS_TARGET, true);
        // Add search bundles selector : reuse keys from above
        searchBundlesSelection = formFactory.addDropdownSingleselect("searchBundlesSelection", searchLayoutContainer, bundlesKeys, bundlesValues, null);
        searchBundlesSelection.setLabel("generic.limit.bundles", null);
        searchBundlesSelection.addActionListener(this, FormEvent.ONCHANGE);
        searchBundlesSelection.select(ALL_BUNDLES_IDENTIFYER, true);
        // Add search bundles children switch
        searchBundlesIncludeBundlesChildrenSwitch = formFactory.addCheckboxesHorizontal("searchBundlesIncludeBundlesChildrenSwitch",
                "generic.limit.bundles.includeChildren", searchLayoutContainer, new String[] { KEYS_ENABLED }, new String[] { KEYS_EMPTY }, null);
        searchBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
        searchBundlesIncludeBundlesChildrenSwitch.addActionListener(listener, FormEvent.ONCLICK);
        searchBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
        // Add priority sort switch
        searchBundlesPrioritySortSwitch = formFactory.addCheckboxesHorizontal("searchBundlesPrioritySortSwitch", "generic.sort.by.priority", searchLayoutContainer,
                new String[] { KEYS_ENABLED }, new String[] { KEYS_EMPTY }, null);
        searchBundlesPrioritySortSwitch.select(KEYS_ENABLED, true);
        searchBundlesPrioritySortSwitch.addActionListener(listener, FormEvent.ONCLICK);
        formLayout.add(searchBundlesPrioritySortSwitch);
        // Add button to trigger search
        FormItemContainer searchButtonLayoutContainer = (FormItemContainer) FormLayoutContainer
                .createHorizontalFormLayout("searchButtonLayoutContainer", getTranslator());
        searchLayoutContainer.add(searchButtonLayoutContainer);
        searchListButton = new FormLinkImpl("searchListButton", "searchListButton", "generic.listButton", Link.BUTTON);
        searchButtonLayoutContainer.add(searchListButton);
        searchTranslateButton = new FormLinkImpl("searchTranslateButton", "searchTranslateButton", "generic.translateButton", Link.BUTTON);
        searchButtonLayoutContainer.add(searchTranslateButton);
    }

    /**
     * Internal helper to initialize or update the status bar and other statistical information on the start page
     */
    private void updateStatistics() {
        I18nManager i18nMgr = I18nManager.getInstance();
        // update progress bar with all package values
        int toTranslateCount = i18nMgr.countI18nItems(referenceLocale, null, true);
        int translatedCount = i18nMgr.countI18nItems(targetLocale, null, true);
        progressBar.setMax(toTranslateCount);
        progressBar.setActual(translatedCount);
        // calculate package dependent values for missing keys display
        String missingBundle = missingBundlesSelection.getSelectedKey();
        if (missingBundle.equals(ALL_BUNDLES_IDENTIFYER)) {
            this.flc.contextPut("missingCount", (toTranslateCount - translatedCount));
        } else {
            int missingToTranslateCount = i18nMgr.countI18nItems(referenceLocale, missingBundle, missingBundlesIncludeBundlesChildrenSwitch.isSelected(0));
            int missingTranslatedCount = i18nMgr.countI18nItems(targetLocale, missingBundle, missingBundlesIncludeBundlesChildrenSwitch.isSelected(0));
            this.flc.contextPut("missingCount", (missingToTranslateCount - missingTranslatedCount));
        }
        String existingBundle = existingBundlesSelection.getSelectedKey();
        if (existingBundle.equals(ALL_BUNDLES_IDENTIFYER)) {
            this.flc.contextPut("existingCount", translatedCount);
        } else {
            int existingTranslateCount = i18nMgr.countI18nItems(referenceLocale, existingBundle, existingBundlesIncludeBundlesChildrenSwitch.isSelected(0));
            this.flc.contextPut("existingCount", existingTranslateCount);
        }
        // calculate package dependent values for all keys display
        String allBundle = allBundlesSelection.getSelectedKey();
        if (allBundle.equals(ALL_BUNDLES_IDENTIFYER)) {
            this.flc.contextPut("allCount", toTranslateCount);
        } else {
            int allToTranslateCount = i18nMgr.countI18nItems(referenceLocale, allBundle, allBundlesIncludeBundlesChildrenSwitch.isSelected(0));
            this.flc.contextPut("allCount", allToTranslateCount);
        }
    }

    /**
     * Override text labels for all GUI elements to use the customizing texts instead of the translation texts
     */
    private void setCustomizingTextLabels() {
        I18nManager i18nMgr = I18nManager.getInstance();
        referenceLangSelection.setLabel("start.customize.referenceLangSelection", null);
        targetLangSelection.setLabel("start.customize.targetLangSelection", null);
        int bundlesCount = i18nMgr.countBundles(null, true);
        progressBar.setUnitLabel(translate("start.customize.progressBar.unitLabel", bundlesCount + ""));
        missingTranslateButton.setI18nKey("generic.customize.translateButton");
        allTranslateButton.setI18nKey("generic.customize.translateButton");
        searchTranslateButton.setI18nKey("generic.customize.translateButton");

    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        I18nManager i18nMgr = I18nManager.getInstance();
        if (source == targetLangSelection) {
            String langKey = targetLangSelection.getSelectedKey();
            targetLocale = i18nMgr.getLocaleOrNull(langKey);
            // Add lang key for image - don't use customizing lang key
            if (customizingMode) {
                this.flc.contextPut("targetLangKey", i18nMgr.createOrigianlLocaleKeyForOverlay(targetLocale));
            } else {
                this.flc.contextPut("targetLangKey", targetLocale.toString());
            }
            updateStatistics();

        } else if (source == referenceLangSelection) {
            String langKey = referenceLangSelection.getSelectedKey();
            referenceLocale = i18nMgr.getLocaleOrNull(langKey);
            // update in gui prefs
            Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
            guiPrefs.putAndSave(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_REFERENCE_LANG, referenceLocale.toString());
            // update GUI
            this.flc.contextPut("referenceLangKey", i18nMgr.getLocaleKey(referenceLocale));
            // Set target language to reference language when in customizing mode
            if (customizingMode) {
                targetLocale = I18nModule.getOverlayLocales().get(referenceLocale);
                targetLangSelection.select(i18nMgr.getLocaleKey(targetLocale), true);
                this.flc.contextPut("targetLangKey", i18nMgr.getLocaleKey(referenceLocale));
            }
            updateStatistics();

        } else if (source == missingListButton || source == missingTranslateButton) {
            String bundle = missingBundlesSelection.getSelectedKey();
            if (bundle.equals(ALL_BUNDLES_IDENTIFYER))
                bundle = null;
            boolean includeBundlesChildren = missingBundlesIncludeBundlesChildrenSwitch.isSelected(0);
            // use the fallback locale because it won't find the key if not already translated in the searchLocale
            List<I18nItem> i18nItems = i18nMgr.findMissingI18nItems(I18nModule.getFallbackLocale(), targetLocale, bundle, includeBundlesChildren);
            boolean prioSortEnabled = missingBundlesPrioritySortSwitch.isSelected(0);
            i18nMgr.sortI18nItems(i18nItems, prioSortEnabled, prioSortEnabled);
            deactivateAndDisposeChildCrumbController();
            // first the list controller
            TranslationToolI18nItemListCrumbController i18nItemlistCrumbCtr = new TranslationToolI18nItemListCrumbController(ureq, getWindowControl(), i18nItems,
                    referenceLocale, customizingMode);
            activateAndListenToChildCrumbController(i18nItemlistCrumbCtr);
            // second the edit controller
            if (source == missingTranslateButton) {
                TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems,
                        referenceLocale, customizingMode);
                i18nItemlistCrumbCtr.activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);
            }

        } else if (source == existingListButton || source == existingTranslateButton) {
            String bundle = existingBundlesSelection.getSelectedKey();
            if (bundle.equals(ALL_BUNDLES_IDENTIFYER))
                bundle = null;
            boolean includeBundlesChildren = existingBundlesIncludeBundlesChildrenSwitch.isSelected(0);
            List<I18nItem> i18nItems = i18nMgr.findExistingI18nItems(targetLocale, bundle, includeBundlesChildren);
            boolean prioSortEnabled = existingBundlesPrioritySortSwitch.isSelected(0);
            i18nMgr.sortI18nItems(i18nItems, prioSortEnabled, prioSortEnabled);
            deactivateAndDisposeChildCrumbController();
            // first the list controller
            TranslationToolI18nItemListCrumbController i18nItemlistCrumbCtr = new TranslationToolI18nItemListCrumbController(ureq, getWindowControl(), i18nItems,
                    referenceLocale, customizingMode);
            activateAndListenToChildCrumbController(i18nItemlistCrumbCtr);
            // second the edit controller
            if (source == existingTranslateButton) {
                TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems,
                        referenceLocale, customizingMode);
                i18nItemlistCrumbCtr.activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);
            }

        } else if (source == allListButton || source == allTranslateButton) {
            String bundle = allBundlesSelection.getSelectedKey();
            if (bundle.equals(ALL_BUNDLES_IDENTIFYER))
                bundle = null;
            boolean includeBundlesChildren = allBundlesIncludeBundlesChildrenSwitch.isSelected(0);
            List<I18nItem> i18nItems = i18nMgr.findExistingAndMissingI18nItems(referenceLocale, targetLocale, bundle, includeBundlesChildren);
            boolean prioSortEnabled = allBundlesPrioritySortSwitch.isSelected(0);
            i18nMgr.sortI18nItems(i18nItems, prioSortEnabled, prioSortEnabled);
            deactivateAndDisposeChildCrumbController();
            // first the list controller
            TranslationToolI18nItemListCrumbController i18nItemlistCrumbCtr = new TranslationToolI18nItemListCrumbController(ureq, getWindowControl(), i18nItems,
                    referenceLocale, customizingMode);
            activateAndListenToChildCrumbController(i18nItemlistCrumbCtr);
            // second the edit controller
            if (source == allTranslateButton) {
                TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems,
                        referenceLocale, customizingMode);
                i18nItemlistCrumbCtr.activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);
            }

        } else if (source == searchListButton || source == searchTranslateButton) {
            String bundle = searchBundlesSelection.getSelectedKey();
            if (bundle.equals(ALL_BUNDLES_IDENTIFYER))
                bundle = null;
            boolean includeBundlesChildren = searchBundlesIncludeBundlesChildrenSwitch.isSelected(0);
            String searchString = searchInput.getValue();
            List<I18nItem> i18nItems;
            Locale searchLocale = (searchReferenceTargetSelection.getSelectedKey().equals(KEYS_REFERENCE) ? referenceLocale : targetLocale);
            if (searchKeyValueSelection.getSelectedKey().equals(KEYS_KEY)) {
                // use the fallback locale because it won't find the key if not already translated in the searchLocale
                i18nItems = i18nMgr.findI18nItemsByKeySearch(searchString, I18nModule.getFallbackLocale(), targetLocale, bundle, includeBundlesChildren);
            } else {
                i18nItems = i18nMgr.findI18nItemsByValueSearch(searchString, searchLocale, targetLocale, bundle, includeBundlesChildren);
            }
            boolean prioSortEnabled = searchBundlesPrioritySortSwitch.isSelected(0);
            I18nManager.getInstance().sortI18nItems(i18nItems, prioSortEnabled, prioSortEnabled);
            deactivateAndDisposeChildCrumbController();
            // first the list controller
            TranslationToolI18nItemListCrumbController i18nItemlistCrumbCtr = new TranslationToolI18nItemListCrumbController(ureq, getWindowControl(), i18nItems,
                    referenceLocale, customizingMode);
            activateAndListenToChildCrumbController(i18nItemlistCrumbCtr);
            // second the edit controller
            if (source == searchTranslateButton) {
                TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq, getWindowControl(), i18nItems,
                        referenceLocale, customizingMode);
                i18nItemlistCrumbCtr.activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);
            }

        } else if (source == missingBundlesSelection) {
            if (missingBundlesSelection.getSelectedKey().equals(ALL_BUNDLES_IDENTIFYER)) {
                missingBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
                missingBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
            } else {
                missingBundlesIncludeBundlesChildrenSwitch.setEnabled(true);
            }
            updateStatistics();

        } else if (source == existingBundlesSelection) {
            if (existingBundlesSelection.getSelectedKey().equals(ALL_BUNDLES_IDENTIFYER)) {
                existingBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
                existingBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
            } else {
                existingBundlesIncludeBundlesChildrenSwitch.setEnabled(true);
            }
            updateStatistics();

        } else if (source == allBundlesSelection) {
            if (allBundlesSelection.getSelectedKey().equals(ALL_BUNDLES_IDENTIFYER)) {
                allBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
                allBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
            } else {
                allBundlesIncludeBundlesChildrenSwitch.setEnabled(true);
            }
            updateStatistics();

        } else if (source == searchBundlesSelection) {
            if (searchBundlesSelection.getSelectedKey().equals(ALL_BUNDLES_IDENTIFYER)) {
                searchBundlesIncludeBundlesChildrenSwitch.select(KEYS_ENABLED, true);
                searchBundlesIncludeBundlesChildrenSwitch.setEnabled(false);
            } else {
                searchBundlesIncludeBundlesChildrenSwitch.setEnabled(true);
            }
            updateStatistics();

        } else if (source == missingBundlesIncludeBundlesChildrenSwitch || source == existingBundlesIncludeBundlesChildrenSwitch
                || source == allBundlesIncludeBundlesChildrenSwitch) {
            updateStatistics();

        } else if (source == missingBundlesPrioritySortSwitch) {
            boolean enabled = missingBundlesPrioritySortSwitch.isSelected(0);
            List<String> bundleNames = I18nModule.getBundleNamesContainingI18nFiles();
            String[] bundlesKeys = buildBundleArrayKeys(bundleNames, enabled);
            String[] bundlesValues = buildBundleArrayValues(bundleNames, enabled);
            missingBundlesSelection.setKeysAndValues(bundlesKeys, bundlesValues, null);

        } else if (source == existingBundlesPrioritySortSwitch) {
            boolean enabled = existingBundlesPrioritySortSwitch.isSelected(0);
            List<String> bundleNames = I18nModule.getBundleNamesContainingI18nFiles();
            String[] bundlesKeys = buildBundleArrayKeys(bundleNames, enabled);
            String[] bundlesValues = buildBundleArrayValues(bundleNames, enabled);
            existingBundlesSelection.setKeysAndValues(bundlesKeys, bundlesValues, null);

        } else if (source == allBundlesPrioritySortSwitch) {
            boolean enabled = allBundlesPrioritySortSwitch.isSelected(0);
            List<String> bundleNames = I18nModule.getBundleNamesContainingI18nFiles();
            String[] bundlesKeys = buildBundleArrayKeys(bundleNames, enabled);
            String[] bundlesValues = buildBundleArrayValues(bundleNames, enabled);
            allBundlesSelection.setKeysAndValues(bundlesKeys, bundlesValues, null);

        } else if (source == searchBundlesPrioritySortSwitch) {
            boolean enabled = searchBundlesPrioritySortSwitch.isSelected(0);
            List<String> bundleNames = I18nModule.getBundleNamesContainingI18nFiles();
            String[] bundlesKeys = buildBundleArrayKeys(bundleNames, enabled);
            String[] bundlesValues = buildBundleArrayValues(bundleNames, enabled);
            searchBundlesSelection.setKeysAndValues(bundlesKeys, bundlesValues, null);

        }

    }

    /*
     * (non-Javadoc)
     */
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (event instanceof I18nItemChangedEvent) {
            I18nItemChangedEvent itemChangedEvent = (I18nItemChangedEvent) event;
            // don't update view directly, wait until view is activated again
            viewIsDirty = true;

        } else if (event == BreadCrumbController.CRUMB_VIEW_ACTIVATED) {
            // update view
            if (viewIsDirty) {
                updateStatistics();
                viewIsDirty = false;
            }
        }
    }

    /**
     * Helper to build a string array from the given bundle names list using optional priority sorting that can be used as keys
     * 
     * @param bundleNames
     * @param prioritySort
     * @return
     */
    private String[] buildBundleArrayKeys(List<String> bundleNames, boolean prioritySort) {
        List<String> bundlesListKeys = new ArrayList<String>();
        bundlesListKeys.add(ALL_BUNDLES_IDENTIFYER);
        if (prioritySort) {
            // clone list, don't sort original list, shared by other users
            List<String> copy = new ArrayList<String>(bundleNames.size());
            for (String bundle : bundleNames) {
                copy.add(bundle);
            }
            I18nManager.getInstance().sortBundles(copy, true);
            bundlesListKeys.addAll(copy);
        } else {
            bundlesListKeys.addAll(bundleNames);
        }
        return ArrayHelper.toArray(bundlesListKeys);
    }

    /**
     * Helper to build a string array from the given bundle names list using optional priority sorting that can be used as values
     * 
     * @param bundleNames
     * @param prioritySort
     * @return
     */
    private String[] buildBundleArrayValues(List<String> bundleNames, boolean prioritySort) {
        List<String> bundlesListValues = new ArrayList<String>();
        bundlesListValues.add(translate("generic.limit.bundles.all"));
        if (prioritySort) {
            // clone list, don't sort original list, shared by other users
            List<String> copy = new ArrayList<String>(bundleNames.size());
            for (String bundle : bundleNames) {
                copy.add(bundle);
            }
            I18nManager.getInstance().sortBundles(copy, true);
            bundlesListValues.addAll(copy);
        } else {
            bundlesListValues.addAll(bundleNames);
        }
        return ArrayHelper.toArray(bundlesListValues);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void formOK(UserRequest ureq) {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     */
    public String getCrumbLinkHooverText() {
        return translate("start.crumb.hoover");
    }

    /*
     * (non-Javadoc)
     */
    public String getCrumbLinkText() {
        if (customizingMode) {
            return translate("start.crumb.link") + " " + translate("start.customize.title");
        }
        return translate("start.crumb.link") + " " + translate("start.title");
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void doDispose() {
        // controller autodisposed by basic controller
    }
}
