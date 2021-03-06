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
package org.olat.presentation.portfolio.structel.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.EPAbstractMap;
import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.EPStructureElement;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.presentation.framework.core.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.presentation.framework.core.control.generic.ajax.tree.MoveTreeNodeEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.presentation.portfolio.structel.EPAddElementsController;
import org.olat.presentation.portfolio.structel.EPArtefactClicked;
import org.olat.presentation.portfolio.structel.EPStructureChangeEvent;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller shows a TOC (table of content) of the given PortfolioStructure elements can be moved around by d&d
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTOCController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final String ARTEFACT_NODE_CLICKED = "artefactNodeClicked";
    private static final String DELETE_LINK_CMD = "delete";
    private static final String ARTEFACT_NODE_IDENTIFIER = "art";
    private static final String ROOT_NODE_IDENTIFIER = "root";
    protected final EPFrontendManager ePFMgr;
    protected final PortfolioStructureDao eSTMgr;
    protected PortfolioStructure rootNode;
    protected final EPSecurityCallback secCallback;
    private final TreeController treeCtr;
    private final VelocityContainer tocV;
    private PortfolioStructure structureClicked;
    private String artefactNodeClicked;

    protected final Map<Long, String> idToPath = new HashMap<Long, String>();
    protected final Map<String, PortfolioStructure> pathToStructure = new HashMap<String, PortfolioStructure>();
    private EPAddElementsController addElCtrl;
    private final Link delButton;

    public EPTOCController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure selectedEl, final PortfolioStructure rootNode,
            final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        this.secCallback = secCallback;
        tocV = createVelocityContainer("toc");
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        eSTMgr = (PortfolioStructureDao) CoreSpringFactory.getBean(PortfolioStructureDao.class);
        this.rootNode = rootNode;
        final AjaxTreeModel treeModel = buildTreeModel();
        treeCtr = new TreeController(ureq, getWindowControl(), translate("toc.root"), treeModel, "myjsCallback");
        treeCtr.setTreeSorting(false, false, false);
        listenTo(treeCtr);
        tocV.put("tocTree", treeCtr.getInitialComponent());
        delButton = LinkFactory.createCustomLink("deleteButton", DELETE_LINK_CMD, "&nbsp;&nbsp;&nbsp;", Link.NONTRANSLATED, tocV, this);
        delButton.setTooltip(translate("deleteButton"), false);
        delButton.setCustomEnabledLinkCSS("b_delete_icon b_eportfolio_del_link ");
        tocV.put("deleteButton", delButton);

        if (selectedEl == null) {
            treeCtr.selectPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey()); // select map
            refreshAddElements(ureq, rootNode);
        } else {
            final String pagePath = calculatePathByDeepestNode(selectedEl);
            treeCtr.selectPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey() + pagePath);
            structureClicked = selectedEl;
            refreshAddElements(ureq, selectedEl);
        }

        putInitialPanel(tocV);
    }

    private String calculatePathByDeepestNode(final PortfolioStructure pStruct) {
        final StringBuffer path = new StringBuffer();
        PortfolioStructure ps = pStruct;
        while (ps.getRootMap() != null) {
            path.insert(0, "/" + ps.getKey().toString());
            ps = ps.getRoot();
        }
        return path.toString();
    }

    protected void refreshTree(final PortfolioStructure root) {
        this.rootNode = root;
        treeCtr.reloadPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey());
    }

    /**
     * refreshing the add elements link to actual structure
     * 
     * @param ureq
     * @param struct
     *            maybe null -> hiding the add-button
     */
    private void refreshAddElements(final UserRequest ureq, final PortfolioStructure struct) {
        tocV.remove(tocV.getComponent("addElement"));
        removeAsListenerAndDispose(addElCtrl);
        if (struct != null) {
            addElCtrl = new EPAddElementsController(ureq, getWindowControl(), struct);
            if (struct instanceof EPPage) {
                if (secCallback.canAddStructure()) {
                    addElCtrl.setShowLink(EPAddElementsController.ADD_STRUCTUREELEMENT);
                }
                if (secCallback.canAddArtefact()) {
                    addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
                }
            } else if (struct instanceof EPAbstractMap) {
                if (secCallback.canAddPage()) {
                    addElCtrl.setShowLink(EPAddElementsController.ADD_PAGE);
                }
            } else { // its a structure element
                if (secCallback.canAddArtefact()) {
                    addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
                }
            }
            listenTo(addElCtrl);
            tocV.put("addElement", addElCtrl.getInitialComponent());
        }
    }

    private AjaxTreeModel buildTreeModel() {
        idToPath.put(rootNode.getKey(), "/" + ROOT_NODE_IDENTIFIER);

        final AjaxTreeModel model = new AjaxTreeModel(ROOT_NODE_IDENTIFIER) {

            @Override
            public List<AjaxTreeNode> getChildrenFor(final String nodeId) {
                final List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
                AjaxTreeNode child;
                boolean isRoot = false;
                PortfolioStructure selStruct = null;
                try {
                    List<PortfolioStructure> structs = new ArrayList<PortfolioStructure>();
                    if (nodeId.equals(ROOT_NODE_IDENTIFIER)) {
                        structs.add(rootNode);
                        isRoot = true;
                    } else if (!nodeId.startsWith(ARTEFACT_NODE_IDENTIFIER)) {
                        selStruct = ePFMgr.loadPortfolioStructureByKey(new Long(nodeId));
                        structs = ePFMgr.loadStructureChildren(selStruct);
                    } else {
                        // its an artefact -> no childs anymore
                        return null;
                    }
                    if (structs != null && structs.size() != 0) {
                        for (final PortfolioStructure portfolioStructure : structs) {
                            final String childNodeId = String.valueOf(portfolioStructure.getKey());
                            final boolean hasStructureChild = eSTMgr.countStructureChildren(portfolioStructure) > 0;
                            final boolean hasArtefacts = eSTMgr.countArtefacts(portfolioStructure) > 0;
                            final boolean hasChilds = hasStructureChild || hasArtefacts;
                            child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle());
                            if (log.isDebugEnabled()) {
                                child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle() + "drop:" + !isRoot + "drag:" + !isRoot + "leaf:" + !hasChilds);
                            }
                            // seems to be a bug, nothing can be dropped on a leaf, therefore we need to tweak with expanded/expandable ourself!
                            // child.put(AjaxTreeNode.CONF_LEAF, !hasChilds);
                            child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, !hasChilds);
                            child.put(AjaxTreeNode.CONF_ALLOWDRAG, !isRoot);

                            child.put(AjaxTreeNode.CONF_EXPANDED, hasStructureChild);
                            child.put(AjaxTreeNode.CONF_EXPANDABLE, hasChilds);
                            child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
                            child.put(AjaxTreeNode.CONF_ISTARGET, !isRoot);

                            child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, portfolioStructure.getIcon());
                            final String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(portfolioStructure.getDescription());
                            child.put(AjaxTreeNode.CONF_QTIP, description);
                            children.add(child);

                            String path;
                            if (isRoot) {
                                path = "/" + ROOT_NODE_IDENTIFIER;
                            } else {
                                path = idToPath.get(selStruct.getKey());
                            }

                            idToPath.put(portfolioStructure.getKey(), path + "/" + childNodeId);
                        }
                    }
                    if (selStruct != null && ePFMgr.countArtefactsRecursively(selStruct) != 0) {
                        final List<AbstractArtefact> artList = ePFMgr.getArtefacts(selStruct);
                        for (final AbstractArtefact abstractArtefact : artList) {
                            // include struct also, to still be unique if an artefact is linked multiple times
                            final String childNodeId = ARTEFACT_NODE_IDENTIFIER + String.valueOf(selStruct.getKey()) + "_" + String.valueOf(abstractArtefact.getKey());
                            child = new AjaxTreeNode(childNodeId, abstractArtefact.getTitle());
                            child.put(AjaxTreeNode.CONF_LEAF, true);
                            child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
                            child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
                            child.put(AjaxTreeNode.CONF_EXPANDED, false);
                            child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
                            child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, getArtefactHandler(abstractArtefact));
                            final String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(abstractArtefact.getDescription());
                            child.put(AjaxTreeNode.CONF_QTIP, description);
                            children.add(child);

                            final String path = idToPath.get(selStruct.getKey());

                            final String artefactPath = path + "/" + childNodeId;
                            idToPath.put(abstractArtefact.getKey(), artefactPath);
                            pathToStructure.put(artefactPath, selStruct);
                        }
                    }
                } catch (final JSONException e) {
                    throw new OLATRuntimeException("Error while creating tree model for map/page/structure selection", e);
                }
                return children;
            }
        };
        model.setCustomRootIconCssClass("o_st_icon");
        return model;
    }

    private EPArtefactHandler<?> getArtefactHandler(final AbstractArtefact artefact) {
        final PortfolioAbstractHandler portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        return portfolioModule.getArtefactHandler(artefact);
    }

    public void update(UserRequest ureq, final PortfolioStructure structure) {
        final String path = idToPath.get(structure.getKey());
        if (path != null) {
            treeCtr.reloadPath(path);
            treeCtr.selectPath(path);
        }
        refreshAddElements(ureq, structure);
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            final Link link = (Link) source;
            if (link.getCommand().equals(DELETE_LINK_CMD)) {
                if (artefactNodeClicked != null) {
                    final AbstractArtefact artefact = ePFMgr.loadArtefactByKey(new Long(getArtefactIdFromNodeId(artefactNodeClicked)));
                    final PortfolioStructure parentStruct = ePFMgr.loadPortfolioStructureByKey(new Long(getArtefactParentStructIdFromNodeId(artefactNodeClicked)));
                    ePFMgr.removeArtefactFromStructure(artefact, parentStruct);
                    // refresh the view
                    fireEvent(ureq, Event.CHANGED_EVENT);
                } else if (structureClicked != null) {
                    if ((structureClicked instanceof EPPage) && !(structureClicked instanceof EPAbstractMap)) {
                        PortfolioStructure ps = structureClicked;
                        while (ePFMgr.loadStructureParent(ps) != null) {
                            ps = ePFMgr.loadStructureParent(ps);
                        }
                        final int childPages = ePFMgr.countStructureChildren(ps);
                        if (childPages > 1) {
                            eSTMgr.removeStructureRecursively(structureClicked);
                            // refresh the view
                            fireEvent(ureq, Event.CHANGED_EVENT);
                        } else {
                            showError("last.page.not.deletable");
                        }
                    } else if (structureClicked instanceof EPStructureElement && !(structureClicked instanceof EPAbstractMap)) {
                        // structures should always be deletable
                        eSTMgr.removeStructureRecursively(structureClicked);
                        // refresh the view
                        fireEvent(ureq, Event.CHANGED_EVENT);
                    } else {
                        showInfo("element.not.deletable");
                    }
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof TreeNodeClickedEvent) {
            resetClickedNodes();
            final TreeNodeClickedEvent treeEv = (TreeNodeClickedEvent) event;
            final String nodeClicked = treeEv.getNodeId();
            final boolean isArtefactNode = nodeClicked.startsWith(ARTEFACT_NODE_IDENTIFIER);
            if (!nodeClicked.equals(ROOT_NODE_IDENTIFIER) && !isArtefactNode) {
                structureClicked = ePFMgr.loadPortfolioStructureByKey(new Long(nodeClicked));
                refreshAddElements(ureq, structureClicked);
                delButton.setVisible(true);
                // send event to load this page
                fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.SELECTED, structureClicked));
                // needed because refreshAddElements set flc dirty, therefore selected node gets lost
                final String path = idToPath.get(structureClicked.getKey());
                treeCtr.selectPath(path);
            } else if (isArtefactNode) {
                artefactNodeClicked = nodeClicked;
                refreshAddElements(ureq, null);
                delButton.setVisible(true);
                final String artIdent = getArtefactIdFromNodeId(nodeClicked);
                final String path = idToPath.get(new Long(artIdent));
                final PortfolioStructure structure = pathToStructure.get(path);
                fireEvent(ureq, new EPArtefactClicked(ARTEFACT_NODE_CLICKED, structure));
                // needed because refreshAddElements set flc dirty, therefore selected node gets lost
                treeCtr.selectPath(path);
            } else {
                // root tree node clicked, no add/delete link
                delButton.setVisible(false);
                refreshAddElements(ureq, null);
                fireEvent(ureq, new Event(ARTEFACT_NODE_CLICKED));
            }
        } else if (event instanceof MoveTreeNodeEvent) {
            resetClickedNodes();
            final MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
            final String movedNode = moveEvent.getNodeId();
            final String oldParent = moveEvent.getOldParentNodeId();
            final String newParent = moveEvent.getNewParentNodeId();
            int newPos = moveEvent.getPosition();
            final boolean isArtefactNode = movedNode.startsWith(ARTEFACT_NODE_IDENTIFIER);
            if (isArtefactNode) {
                final String nodeId = getArtefactIdFromNodeId(movedNode);
                if (checkNewArtefactTarget(nodeId, newParent)) {
                    if (moveArtefactToNewParent(nodeId, oldParent, newParent)) {
                        if (log.isDebugEnabled()) {
                            log.info("moved artefact " + nodeId + " from structure " + oldParent + " to " + newParent, null);
                        }
                        moveEvent.setResult(true, null, null);
                        // refresh the view
                        final EPMoveEvent movedEvent = new EPMoveEvent(newParent, nodeId);
                        fireEvent(ureq, movedEvent);
                    } else {
                        moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.move"));
                    }
                } else {
                    moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.target"));
                }
            } else {
                if (checkNewStructureTarget(movedNode, oldParent, newParent)) {
                    if (moveStructureToNewParent(movedNode, oldParent, newParent, newPos)) {
                        if (log.isDebugEnabled()) {
                            log.debug("moved structure " + movedNode + " from structure " + oldParent + " to " + newParent, null);
                        }
                        moveEvent.setResult(true, null, null);
                        // refresh the view
                        final EPMoveEvent movedEvent = new EPMoveEvent(newParent, movedNode);
                        fireEvent(ureq, movedEvent);
                    } else {
                        moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.move"));
                    }
                } else {
                    moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.target"));
                }

            }
        } else if (source == addElCtrl) {
            // refresh the view, this is a EPStructureChangeEvent
            fireEvent(ureq, event);
        }
    }

    // reset previously choosen nodes. reference were there to be able to delete a node.
    private void resetClickedNodes() {
        structureClicked = null;
        artefactNodeClicked = null;
    }

    private String getArtefactIdFromNodeId(final String nodeId) {
        String artId = nodeId.substring(ARTEFACT_NODE_IDENTIFIER.length());
        if (artId.contains("_")) {
            artId = artId.substring(artId.indexOf("_") + 1);
        }
        return artId;
    }

    private String getArtefactParentStructIdFromNodeId(final String nodeId) {
        String structId = nodeId.substring(ARTEFACT_NODE_IDENTIFIER.length());
        if (structId.contains("_")) {
            structId = structId.substring(0, structId.indexOf("_"));
        }
        return structId;
    }

    /**
     * check if an artefact might be moved to this new parent node artefact might be moved to pages or structureElements, but not on maps
     * 
     * @param artefactId
     * @param structureId
     * @return
     */
    private boolean checkNewArtefactTarget(final String artefactId, final String structureId) {
        // artefact cannot be moved directly under root
        if (ROOT_NODE_IDENTIFIER.equals(structureId)) {
            return false;
        }

        PortfolioStructure newParStruct;
        AbstractArtefact artefact;
        try {
            artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
            newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(structureId));
        } catch (final Exception e) {
            log.warn("could not check for valid artefact target", e);
            return false;
        }
        final boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
        if (sameTarget) {
            return false;
        }
        if (newParStruct instanceof EPAbstractMap) {
            return false;
        }
        return true;
    }

    // really do the move!
    private boolean moveArtefactToNewParent(final String artefactId, final String oldParentId, final String newParentId) {
        PortfolioStructure newParStruct;
        PortfolioStructure oldParStruct;
        AbstractArtefact artefact;
        try {
            artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
            oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParentId));
            newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParentId));
        } catch (final Exception e) {
            log.error("could not load artefact, old and new parent", e);
            return false;
        }
        return ePFMgr.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct);
    }

    /**
     * check if a structure (page/structEl/map may be dropped here! its only allowed to move: - StructureElement from page -> page - change the order of pages - change
     * the order of structures
     * 
     * @param subjectStructId
     * @param oldParStructId
     * @param newParStructId
     * @return
     */
    private boolean checkNewStructureTarget(final String subjectStructId, final String oldParStructId, final String newParStructId) {
        PortfolioStructure structToBeMvd;
        PortfolioStructure newParStruct;
        if (oldParStructId.equals(newParStructId))
            return true; // seems only to be a move in order
        if (newParStructId.equals(ROOT_NODE_IDENTIFIER))
            return false;
        try {
            structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
            newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
        } catch (final Exception e) {
            log.error("could not check for valid structure target", e);
            return false;
        }
        if (structToBeMvd instanceof EPPage && newParStruct instanceof EPPage)
            return false;
        if (structToBeMvd instanceof EPStructureElement && !(newParStruct instanceof EPPage))
            return false;

        return true;
    }

    // really do the move
    private boolean moveStructureToNewParent(final String subjectStructId, final String oldParStructId, final String newParStructId, int newPos) {
        PortfolioStructure structToBeMvd;
        PortfolioStructure oldParStruct;
        PortfolioStructure newParStruct;
        try {
            structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
            oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParStructId));
            newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
        } catch (final Exception e) {
            log.error("could not load: structure to be moved, old or new structure while trying to move", e);
            return false;
        }

        if (oldParStructId.equals(newParStructId)) {
            // this is only a position move
            return ePFMgr.moveStructureToPosition(structToBeMvd, newPos);
        }

        return ePFMgr.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, newPos);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
