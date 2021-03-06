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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.layout.fullWebApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLoggerInstaller;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.GUIMessage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.ChiefControllerMessageEvent;
import org.olat.presentation.framework.core.chiefcontrollers.LanguageChangedEvent;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.components.htmlheader.jscss.CustomCSS;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.OncePanel;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.VetoableCloseController;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.WindowControlInfoImpl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.guistack.GuiStack;
import org.olat.presentation.framework.core.control.info.WindowControlInfo;
import org.olat.presentation.framework.core.control.navigation.BornSiteInstance;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.framework.core.control.util.ZIndexWrapper;
import org.olat.presentation.framework.layout.fullWebApp.util.GlobalStickyMessage;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The BaseFullWebappController defines the outer most part of the main layout
 * <P>
 * Initial Date: 20.07.2007 <br>
 * 
 * @author patrickb, Felix Jost, Florian Gnägi
 */
public class BaseFullWebappController extends BasicController implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String PRESENTED_AFTER_LOGIN_WORKFLOW = "presentedAfterLoginWorkflow";
    public static final String CURRENT_CUSTOM_CSS_KEY = "currentcustomcss";

    // STARTED
    private GuiStack currentGuiStack;
    private Panel main;
    private Panel modalPanel;
    private GUIMessage guiMessage;
    private OncePanel guimsgPanel;
    private Panel guimsgHolder;
    private Panel currentMsgHolder;
    private VelocityContainer guimsgVc;

    private VelocityContainer mainVc, navVc;

    // NEW FROM FullChiefController
    private Controller headerCtr, topnavCtr, footerCtr;
    private SiteInstance curSite;
    private DTab curDTab;
    private SiteInstance prevSite;

    // the dynamic tabs list
    private List<DTab> dtabs;
    private List<String> dtabsLinkNames;
    private List<Controller> dtabsControllers;
    // used as link id which is load url safe (e.g. replayable
    private int dtabCreateCounter = 0;
    // the sites list
    private List<SiteInstance> sites;
    private Map<SiteInstance, BornSiteInstance> siteToBornSite = new HashMap<SiteInstance, BornSiteInstance>();
    private static final int MAX_TABS = 5;
    private int navLinkCounter = 1;

    private BaseFullWebappControllerParts baseFullWebappControllerParts;
    protected Controller contentCtrl;
    private Panel initialPanel;
    private DTabs dTabsImpl;

    public BaseFullWebappController(UserRequest ureq, WindowControl ouisc_wControl, BaseFullWebappControllerParts baseFullWebappControllerParts) {
        // only-use-in-super-call, since we define our own
        super(ureq, null);

        this.baseFullWebappControllerParts = baseFullWebappControllerParts;

        guiMessage = new GUIMessage();
        guimsgPanel = new OncePanel("guimsgPanel");

        // define the new windowcontrol
        final WindowControl origWCo = ouisc_wControl;

        WindowControl myWControl = new WindowControl() {
            private WindowControlInfo wci;

            {
                wci = new WindowControlInfoImpl(BaseFullWebappController.this, origWCo.getWindowControlInfo());
            }

            /**
			 */
            @Override
            @SuppressWarnings("synthetic-access")
            public void pushToMainArea(Component newMainArea) {
                currentGuiStack.pushContent(newMainArea);
            }

            /**
			 */

            @Override
            @SuppressWarnings("synthetic-access")
            public void pushAsModalDialog(Component newModalDialog) {
                currentGuiStack.pushModalDialog(newModalDialog);
            }

            /**
			 */
            @Override
            @SuppressWarnings("synthetic-access")
            public void pop() {
                // reactivate latest dialog from stack, dumping current one
                currentGuiStack.popContent();
            }

            /**
			 */
            @Override
            @SuppressWarnings("synthetic-access")
            public void setInfo(String info) {
                guiMessage.setInfo(info);
                guimsgPanel.setContent(guimsgVc);

                // setInfo is called input guimsgPanel into the correct place
            }

            /**
			 */
            @Override
            @SuppressWarnings("synthetic-access")
            public void setError(String error) {
                guiMessage.setError(error);
                guimsgPanel.setContent(guimsgVc);
            }

            /**
			 */
            @Override
            @SuppressWarnings("synthetic-access")
            public void setWarning(String warning) {
                guiMessage.setWarn(warning);
                guimsgPanel.setContent(guimsgVc);
            }

            @Override
            public WindowControlInfo getWindowControlInfo() {
                return wci;
            }

            @Override
            public void makeFlat() {
                throw new AssertException("should never be called!");
            }

            @Override
            public BusinessControl getBusinessControl() {
                return origWCo.getBusinessControl();
            }

            @Override
            public WindowBackOffice getWindowBackOffice() {
                return origWCo.getWindowBackOffice();
            }

        };
        overrideWindowControl(myWControl);
        dTabsImpl = new DTabsImpl();

        Window myWindow = myWControl.getWindowBackOffice().getWindow();
        myWindow.setAttribute("DTabs", dTabsImpl);
        // REVIEW:PB remove if back support is desired
        myWindow.addListener(this);// to be able to report BACK / FORWARD / RELOAD
        /*
         * use getAttribute on i.e. Windows(ureq).getWindow(ureq) to retrieve this "service"
         */

        /*
         * does all initialisation, moved to method because of possibility to react on LanguageChangeEvents -> resets and rebuilds footer, header, topnav, sites, content
         * etc.
         */
        initialize(ureq);

        initialPanel = putInitialPanel(mainVc);
        // ------ all the frame preparation is finished ----

        if (CoreSpringFactory.containsBean("fullWebApp.AfterLoginInterceptionControllerCreator")) {
            // present an overlay with configured afterlogin-controllers or nothing if none configured.
            // presented only once per session.
            Boolean alreadySeen = ((Boolean) ureq.getUserSession().getEntry(PRESENTED_AFTER_LOGIN_WORKFLOW));
            if (ureq.getUserSession().isAuthenticated() && alreadySeen == null) {
                Controller aftLHookCtr = ((ControllerCreator) CoreSpringFactory.getBean("fullWebApp.AfterLoginInterceptionControllerCreator")).createController(ureq,
                        getWindowControl());
                aftLHookCtr.getInitialComponent();
                ureq.getUserSession().putEntry(PRESENTED_AFTER_LOGIN_WORKFLOW, Boolean.TRUE);
            }
        }

        /*
         * register for cycle event to be able to adjust the guimessage place
         */
        getWindowControl().getWindowBackOffice().addCycleListener(this);
        /*
         * register for locale change events ->
         */
        // move to a i18nModule? languageManger? languageChooserController?
        OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
        ureq.getUserSession().getSingleUserEventCenter().registerFor(this, getIdentity(), wrappedLocale);
        /*
         * register for global sticky message changed events
         */
        GlobalStickyMessage.registerForGlobalStickyMessage(this, ureq.getIdentity());

    }

    private void initialize(UserRequest ureq) {
        mainVc = createVelocityContainer("fullwebapplayout");
        // use separate container for navigation to prevent full page refresh in ajax mode on site change
        // nav is not a controller part because it is a fundamental part of the BaseFullWebAppConroller.
        navVc = createVelocityContainer("nav");
        mainVc.put("navComponent", navVc);

        // GUI messages
        guimsgVc = createVelocityContainer("guimsg");
        // FIXME fg: create controller that uses monolog controller after monolog
        // controller refactoring. is all the same...

        guimsgVc.contextPut("guiMessage", guiMessage);
        guimsgHolder = new Panel("guimsgholder");
        guimsgHolder.setContent(guimsgPanel);
        currentMsgHolder = guimsgHolder;

        mainVc.put("guimessage", guimsgHolder);

        dtabs = new CopyOnWriteArrayList<DTab>();
        dtabsLinkNames = new ArrayList<String>();
        dtabsControllers = new ArrayList<Controller>();

        // -- sites -- by definition the first site is activated at the beginning
        sites = baseFullWebappControllerParts.getSiteInstances(ureq, getWindowControl());
        if (sites != null && sites.size() == 0) {
            sites = null;
        }
        // either sites is null or contains at least one SiteInstance.
        if (sites != null) {
            // create the links for the sites
            for (Iterator<SiteInstance> iterator = sites.iterator(); iterator.hasNext();) {
                SiteInstance si = iterator.next();
                Link link = LinkFactory.createCustomLink("t" + navLinkCounter, "t", "", Link.NONTRANSLATED, navVc, this);
                link.setCustomDisplayText(si.getNavElement().getTitle());
                link.setTitle(si.getNavElement().getDescription());
                link.setUserObject(si);
                Character accessKey = si.getNavElement().getAccessKey();
                if (accessKey != null && StringHelper.containsNonWhitespace(accessKey.toString())) {
                    link.setAccessKey(accessKey.toString());
                }
                navLinkCounter++;
            }
        }

        navVc.contextPut("sites", sites);
        navVc.contextPut("dtabs", dtabs);
        navVc.contextPut("dtabsLinkNames", dtabsLinkNames);
        navVc.contextPut("tabhelper", this);

        // ----------- header, optional (e.g. for logo, advertising )
        headerCtr = baseFullWebappControllerParts.createHeaderController(ureq, getWindowControl());
        if (headerCtr != null) {
            listenTo(headerCtr); // cleanup on dispose
            mainVc.put("headerComponent", headerCtr.getInitialComponent());
        }

        // ----------- topnav, optional (e.g. for imprint, logout)
        topnavCtr = baseFullWebappControllerParts.createTopNavController(ureq, getWindowControl());
        if (topnavCtr != null) {
            listenTo(topnavCtr); // cleanup on dispose
            mainVc.put("topnavComponent", topnavCtr.getInitialComponent());
        }

        // ----------- nav, optional (e.g. site navigation with tabs)
        // TODO fg: refactor to its own component // Review-OLD:(pb) should then go also
        // into ..Parts?!

        // panel for modal overlays, placed right after the olat-header-div
        modalPanel = new Panel("ccmodalpanel");
        mainVc.put("modalpanel", modalPanel);

        // ----------- main, mandatory (e.g. a LayoutMain3ColsController)
        // ------------------
        main = new Panel("main");
        mainVc.put("main", main);

        // ----------- footer, optional (e.g. for copyright, powered by)
        footerCtr = baseFullWebappControllerParts.createFooterController(ureq, getWindowControl());
        if (footerCtr != null) {
            listenTo(footerCtr); // cleanup on dispose
            mainVc.put("footerComponent", footerCtr.getInitialComponent());
        }

        contentCtrl = baseFullWebappControllerParts.getContentController(ureq, getWindowControl());
        if (contentCtrl != null) {
            listenTo(contentCtrl);
            GuiStack gs = getWindowControl().getWindowBackOffice().createGuiStack(contentCtrl.getInitialComponent());
            setGuiStack(gs);
            main.setContent(contentCtrl.getInitialComponent());
        }
        if (sites != null) {
            // ------ activate now main
            prevSite = sites.get(0);
            if (contentCtrl == null) {
                // activate site only if no content was set -> allow content before activation of default site.
                activateSite(sites.get(0), ureq, null);
            }
        }
        if (sites == null && contentCtrl == null) {
            throw new AssertException("either one site has to be present or a content controller");
        }

        // set maintenance message
        String stickyMessage = GlobalStickyMessage.getGlobalStickyMessage();
        this.mainVc.contextPut("hasStickyMessage", (stickyMessage == null ? Boolean.FALSE : Boolean.TRUE));
        this.mainVc.contextPut("stickyMessage", stickyMessage);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source instanceof Link) {
            Link link = (Link) source;
            String mC = link.getCommand().substring(0, 1);
            if (mC.equals("t")) { // activate normal tab
                SiteInstance s = (SiteInstance) link.getUserObject();
                activateSite(s, ureq, null);
            } else if (mC.equals("a")) { // activate dyntab
                DTab dt = (DTab) link.getUserObject();
                doActivateDTab(dt);
            } else if (mC.equals("u")) { // undock dyntab
                // TODO:fj:c look at undock feature
            } else if (mC.equals("c")) { // close dyntab
                DTab dt = (DTab) link.getUserObject();
                requestCloseTab(dt);
            }
        } else if (source == getWindowControl().getWindowBackOffice().getWindow()) {
            // OLAT BACK-FORWARD Handling -> Prevent any back forward until 80% handles back-forward as defined
            // see adjustState
            if (event == Window.OLDTIMESTAMPCALL) {
                // we have a "reload" push or such -> set Warn Msg
                // getWindowControl().setWarning(this.getTranslator().translate("warn.reload"));//

                if (GUIInterna.isLoadPerformanceMode()) {
                    log.info("loadtestMode RELOAD");
                } else {
                    log.info("RELOAD");
                }
            }
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        int tabIndex = dtabsControllers.indexOf(source);
        if (tabIndex > -1) {
            // Event comes from a controller in a dtab. Check if the controller is
            // finished and close the tab. Cancel and failed is interpreted as
            // finished.
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
                DTab tab = dtabs.get(tabIndex);
                dTabsImpl.removeDTab(tab);// disposes also tab and controllers
            }
        }

    }

    @Override
    protected void doDispose() {
        // deregister for chief global sticky messages events
        GlobalStickyMessage.deregisterForGlobalStickyMessage(this);
        // dispose sites and tabs

        if (dtabs != null) {
            for (DTab tab : dtabs) {
                tab.dispose();
            }
            for (BornSiteInstance bornSite : siteToBornSite.values()) {
                bornSite.dispose();
            }
            dtabs = null;
            dtabsControllers = null;
            sites = null;
            siteToBornSite = null;
        }
        // clear the DTabs Service
        Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
        myWindow.removeAttribute("DTabs");

        getWindowControl().getWindowBackOffice().removeCycleListener(this);
    }

    private void setGuiStack(GuiStack guiStack) {
        currentGuiStack = guiStack;
        Panel guiStackPanel = currentGuiStack.getPanel();
        main.setContent(guiStackPanel);
        // place for modal dialogs, which are overlayd over the normal layout (using
        // css alpha blending)
        // maybe null if no current modal dialog -> clears the panel
        Panel modalStackP = currentGuiStack.getModalPanel();
        modalPanel.setContent(modalStackP);
    }

    // FROM FULLCHIEFCONTROLLER

    private void activateSite(SiteInstance s, UserRequest ureq, String viewIdentifier) {
        BornSiteInstance bs = siteToBornSite.get(s);
        GuiStack gs;
        Controller resC;
        // PB//WindowControl site_wControl;
        if (bs != null && s != curSite) {
            // single - click -> fetch guistack from cache
            gs = bs.getGuiStackHandle();
            resC = bs.getController();
            // PB//site_wControl = bs.getWindowControl();
        } else {
            // bs == null (not yet in cache) || s == curSite
            // double click or not yet in cache.
            // dispose old controller
            if (bs != null) {
                // already in cache -> dispose old
                bs.getController().dispose();
            }
            // reset site and create new controller
            s.reset();
            /*
             * PB OLATResourceable ores = OresHelper.createOLATResourceableInstance(SiteInstance.class, Long.valueOf(sites.indexOf(s))); ContextEntry ce =
             * BusinessControlFactory.getInstance().createContextEntry(ores); WindowControl bwControl =
             * BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl()); resC = s.createController(ureq, bwControl);
             */
            resC = s.createController(ureq, getWindowControl());
            gs = getWindowControl().getWindowBackOffice().createGuiStack(resC.getInitialComponent());
            // PB//site_wControl = bwControl;
            // PB//siteToBornSite.put(s, new BornSiteInstance(gs, resC, bwControl));
            siteToBornSite.put(s, new BornSiteInstance(gs, resC));
        }
        doActivateSite(s, gs);

        // set current BusPath for extraction in the TopNav Controller
        // FIXME:pb:2009-06-21:move core
        // PB//getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", site_wControl);
        getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", getWindowControl());

        if (viewIdentifier != null && (resC instanceof Activateable)) {
            Activateable a = (Activateable) resC;
            a.activate(ureq, viewIdentifier);
        }
    }

    private void doActivateSite(SiteInstance s, GuiStack gs) {
        removeCurrentCustomCSSFromView();
        // for state keeping: remember the currently activated site position
        int pos = sites.indexOf(s);
        setState("s" + pos);

        curDTab = null;
        if (curSite != null) {
            prevSite = s;
        }
        curSite = s;
        setGuiStack(gs);
        navVc.contextPut("pageTitle", curSite.getNavElement().getTitle());
        navVc.setDirty(true);
        // add css for this site
        BornSiteInstance bs = siteToBornSite.get(s);
        if (bs != null)
            addCurrentCustomCSSToView(bs.getCustomCSS());
    }

    private void doActivateDTab(DTab dtabi) {

        // System.err.println(">>>>> dynamic site >>>>");
        getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", dtabi.getWindowControl());
        // System.err.println(busPath);
        // System.err.println("wControl:"+dtabi.getWindowControl());
        // System.err.println("<<<<< dynamic site <<<<");

        removeCurrentCustomCSSFromView();
        // for state keeping: remember the ores of the dtabi
        setState("d" + OresHelper.createStringRepresenting(dtabi.getOLATResourceable()));

        curDTab = dtabi;
        curSite = null;
        setGuiStack(dtabi.getGuiStackHandle());
        // set description as page title, getTitel() might contain trucated values
        // TODO:gs:a html escaping or removing should be done everywhere where text input fields are written to velocity. Best would be to not allow it in the input
        // fields or escape it there
        navVc.contextPut("pageTitle", curDTab.getNavElement().getDescription());
        navVc.setDirty(true);
        // add css for this tab
        addCurrentCustomCSSToView(curDTab.getCustomCSS());
    }

    /**
     * Remove the current custom css from the view
     */
    protected void removeCurrentCustomCSSFromView() {
        Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
        CustomCSS currentCustomCSS = (CustomCSS) myWindow.getAttribute(CURRENT_CUSTOM_CSS_KEY);
        if (currentCustomCSS != null) {
            // remove css and js from view
            mainVc.remove(currentCustomCSS.getJSAndCSSComponent());
            myWindow.removeAttribute(CURRENT_CUSTOM_CSS_KEY);
        }
    }

    /**
     * Add a custom css to the view and mark it as the curent custom CSS.
     * 
     * @param customCSS
     */
    protected void addCurrentCustomCSSToView(CustomCSS customCSS) {
        if (customCSS == null)
            return;
        // The current CSS is stored as a window attribute so that is can be
        // accessed by the IFrameDisplayController
        Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
        myWindow.setAttribute(CURRENT_CUSTOM_CSS_KEY, customCSS);
        // add css component to view
        mainVc.put("jsAndCss", customCSS.getJSAndCSSComponent());
    }

    /**
     * @param pos
     * @return the dtab at pos pos
     */
    public DTab getDTabAt(int pos) {
        return dtabs.get(pos);
    }

    /**
     * @param dt
     */
    private void requestCloseTab(DTab dt) {
        Controller c = dt.getController(); // FIXME:fj: test
        // vetoableclosecontroller
        if (c instanceof VetoableCloseController) {
            VetoableCloseController vcc = (VetoableCloseController) c;
            // rembember current dtab, and swap to the temporary one
            DTab reTab = curDTab;
            doActivateDTab(dt);
            boolean immediateClose = vcc.requestForClose();
            if (!immediateClose) {
                return;
            } else {
                if (reTab != null) {
                    doActivateDTab(reTab);
                }
                dTabsImpl.removeDTab(dt);
            }
        } else {
            dTabsImpl.removeDTab(dt);
        }
    }

    @Override
    public void event(Event event) {
        if (event == Window.AFTER_VALIDATING) {
            // now update the guimessage

            List places = (List) getWindowControl().getWindowBackOffice().getData("guimessage");
            Panel winnerP = null;
            int maxZ = -1;
            if (places != null) {
                // we have places where we can put the gui message
                for (Iterator it_places = places.iterator(); it_places.hasNext();) {
                    ZIndexWrapper ziw = (ZIndexWrapper) it_places.next();
                    int cind = ziw.getZindex();
                    if (cind > maxZ) {
                        maxZ = cind;
                        winnerP = ziw.getPanel();
                    }
                }
            } else {
                winnerP = guimsgHolder;
            }

            if (winnerP != currentMsgHolder) {
                currentMsgHolder.setContent(null);
                winnerP.setContent(guimsgPanel);
                currentMsgHolder = winnerP;
            } // else same place, nothing to change
              //
        } else if (event instanceof LanguageChangedEvent) {
            LanguageChangedEvent lce = (LanguageChangedEvent) event;
            getTranslator().setLocale(lce.getNewLocale());
            initialize(lce.getCurrentUreq());
            initialPanel.popContent();
            initialPanel.pushContent(mainVc);
            //
        } else if (event instanceof ChiefControllerMessageEvent) {
            ChiefControllerMessageEvent messageEvent = (ChiefControllerMessageEvent) event;
            // msg can be set to show only on one node or on all nodes
            String msg = GlobalStickyMessage.getGlobalStickyMessage();// either null, or the global message or the per-node-message
            Boolean hasStickyMessage = Boolean.valueOf(msg != null);
            this.mainVc.contextPut("hasStickyMessage", hasStickyMessage);
            this.mainVc.contextPut("stickyMessage", msg != null ? msg : "");
        }
    }

    @Override
    protected void adjustState(ControllerState cstate, UserRequest ureq) {
        String state = cstate.getSerializedState();
        // we always have a defined state, even after init - so don't handle the
        // empty-string-case
        char ch = state.charAt(0);
        switch (ch) {
        case 's': // static site selection
            int pos = Integer.parseInt(state.substring(1));
            SiteInstance s = sites.get(pos);
            activateSite(s, ureq, null);
            break;
        case 'd': // dynamic tab selection
            String oresString = state.substring(1);
            for (int i = 0; i < dtabs.size(); i++) {
                DTab dt = dtabs.get(i);
                if (oresString.equals(OresHelper.createStringRepresenting(dt.getOLATResourceable()))) {
                    doActivateDTab(dt);
                    break;
                }
                // if no matching dtabs were found - do nothing, since that means the
                // tab was closed and we cannot "browserback" to it again.
            }
            break;
        default:
            throw new AssertException("unknown state to handle:" + state);
        }
    }

    /**
     * [used by velocity] helper for velocity
     */
    public boolean isSiteActive(SiteInstance si) {
        return curSite != null && si == curSite;
    }

    /**
     * [used by velocity
     * 
     * @return
     */
    public boolean isDTabActive(DTab dtab) {
        return curDTab != null && dtab == curDTab;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private final class DTabsImpl implements DTabs {

        @Override
        public DTab getDTab(OLATResourceable ores) {
            for (DTab dtab : dtabs) {
                if (OresHelper.equals(dtab.getOLATResourceable(), ores)) {
                    return dtab;
                }
            }
            return null;
        }

        @Override
        public DTab openDTab(OLATResourceable ores, Controller controller, String title) {
            DTab dTab = getDTab(ores);
            if (dTab == null) {
                if (dtabs.size() >= MAX_TABS) {
                    getWindowControl().setError(translate("warn.tabsfull"));
                    return null;
                }

                dTab = new DTab(ores, controller, title, getWindowControl());
                // add to tabs list
                dtabs.add(dTab);
                dtabsLinkNames.add(Integer.toString(dtabCreateCounter));
                Link link = LinkFactory.createCustomLink("a" + dtabCreateCounter, "a" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc, BaseFullWebappController.this);
                link.setCustomDisplayText(dTab.getNavElement().getTitle());
                link.setTitle(dTab.getTitle());
                link.setUserObject(dTab);
                // Set accessibility access key using the 's' key. You can loop through all opened tabs by
                // pressing s repetitively (works only in IE/FF which is normally used by blind people)
                link.setAccessKey("s");
                // add close links
                Link calink = LinkFactory.createCustomLink("ca" + dtabCreateCounter, "ca" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc,
                        BaseFullWebappController.this);
                calink.setCustomEnabledLinkCSS("b_nav_tab_close");
                calink.setTitle(translate("close"));
                calink.setTooltip(translate("close"), false);
                calink.setUserObject(dTab);
                Link cplink = LinkFactory.createCustomLink("cp" + dtabCreateCounter, "cp" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc,
                        BaseFullWebappController.this);
                cplink.setCustomEnabledLinkCSS("b_nav_tab_close");
                cplink.setTitle(translate("close"));
                cplink.setTooltip(translate("close"), false);
                cplink.setUserObject(dTab);

                Controller dtabCtr = dTab.getController();
                dtabCtr.addControllerListener(BaseFullWebappController.this);
                // add to tabs controller lookup table for later event dispatching
                dtabsControllers.add(dtabCtr);
                // increase DTab added counter.
                dtabCreateCounter++;
            }

            // set current BusPath for extraction in the TopNav Controller
            // FIXME:pb:2009-06-21:move core
            getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", getWindowControl());

            return dTab;
        }

        @Override
        public void removeDTab(DTab delt) {
            // remove from tab list and mapper table
            synchronized (dtabs) {// o_clusterOK dtabs are per user session only - user session is always in the same vm
                // make dtabs and dtabsControllers access synchronized
                int dtabIndex = dtabs.indexOf(delt);
                if (dtabIndex == -1) {
                    // OLAT-3343 :: although one session only is implemented, a user can
                    // open multiple "main windows" in different _browser tabs_.
                    // closing one dtab in _browser tab one_ and then closing the same dtab
                    // once again in the _browser tab two_ leads to the case where dtabIndex
                    // is -1, e.g. not found causing the redscreen described in the issue.
                    // TODO:2008-07-25: pb: define concept of "multi windowing" "main windowing"
                    // and reconsider this place.
                    //
                    // NOTHING TO REMOVE, return
                    return;
                }
                // Remove tab itself
                dtabs.remove(delt);
                dtabsLinkNames.remove(dtabIndex);
                Controller tabCtr = dtabsControllers.get(dtabIndex);
                dtabsControllers.remove(tabCtr);

                navVc.setDirty(true);
                // remove created links for dtab out of container
                navVc.remove(navVc.getComponent("a" + delt.hashCode()));
                navVc.remove(navVc.getComponent("ca" + delt.hashCode()));
                navVc.remove(navVc.getComponent("cp" + delt.hashCode()));
                if (delt == curDTab) { // if we close the current tab -> return to the
                    // latest selected static tab
                    // pre: prevSite != null
                    // activate previous chosen static site -> this site has already been
                    // constructed and is thus in the cache
                    SiteInstance si = prevSite;
                    BornSiteInstance bs = siteToBornSite.get(si);
                    // bs != null since clicked previously
                    GuiStack gsh = bs.getGuiStackHandle();
                    doActivateSite(si, gsh);
                } // else just remove the dtabs
                delt.dispose();// dispose tab and controllers in tab
            }
        }

        @Override
        public void activate(final UserRequest ureq, final DTab dTab, final String viewIdentifier) {
            // FIXME:fj:c if viewIdentifier is DTABS.initialView -> activate to this
            // init view (e.g. kurs in run mode, repo-detail-edit...)
            // jump here via external link or just open a new tab from e.g. repository
            Controller c = dTab.getController();
            if (c == null)
                throw new AssertException("no controller set yet! " + dTab + ", view: " + viewIdentifier);
            doActivateDTab(dTab);
            if (viewIdentifier != null && c instanceof Activateable) {
                final Activateable activateable = ((Activateable) c);
                ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {

                    @Override
                    public void run() {
                        activateable.activate(ureq, viewIdentifier);
                    }

                }, activateable.getUserActivityLogger());
            }

            // activating a tab is like focusing a new window - we need to adjust the
            // guipath since e.g. the button triggering the activation is not
            // part of the guipath, but rather the new tab in its initial state.
            // in all other cases the "focus of interest" (where the calculation of the
            // guipath is started) matches the controller which listens to the
            // event caused by a user interaction.
            // WindowBackOffice wboNew = getWindowControl().getWindowBackOffice(); //
            // this is the starting point.
            // getWindowControl().getWindowBackOffice().adjustGuiPathCenter(getWindowControl().getWindowControlInfo());
        }

        @Override
        public void activateStatic(UserRequest ureq, String className, String viewIdentifier) {
            for (SiteInstance site : sites) {
                String cName = site.getClass().getName();
                if (cName.equals(className)) {
                    activateSite(site, ureq, viewIdentifier);
                    return;
                }
            }
        }

        @Override
        public WindowControl getWindowControl() {
            return BaseFullWebappController.this.getWindowControl();
        }

    }

}
