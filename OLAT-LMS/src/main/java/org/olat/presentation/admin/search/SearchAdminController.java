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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.admin.search;

import org.apache.log4j.Logger;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * is the controller for
 * 
 * @author Felix Jost
 */
public class SearchAdminController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    VelocityContainer myContent;
    private final SearchAdminForm searchAdminForm;

    Panel main;
    private final Link stopIndexingButton;
    private final Link startIndexingButton;

    /**
     * @param ureq
     * @param wControl
     */
    public SearchAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        main = new Panel("searchmain");
        myContent = createVelocityContainer("index");
        startIndexingButton = LinkFactory.createButtonSmall("button.startindexing", myContent, this);
        stopIndexingButton = LinkFactory.createButtonSmall("button.stopindexing", myContent, this);
        myContent.contextPut("searchstatus", SearchServiceFactory.getService().getStatus());

        searchAdminForm = new SearchAdminForm(ureq, wControl);
        listenTo(searchAdminForm);

        searchAdminForm.setIndexInterval(SearchServiceFactory.getService().getIndexInterval());

        myContent.put("searchAdminForm", searchAdminForm.getInitialComponent());
        main.setContent(myContent);
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startIndexingButton) {
            SearchServiceFactory.getService().startIndexing();
            log.info("Indexing started via Admin");
            myContent.setDirty(true);
        } else if (source == stopIndexingButton) {
            SearchServiceFactory.getService().stopIndexing();
            log.info("Indexing stopped via Admin");
            myContent.setDirty(true);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchAdminForm) {
            if (event == Event.DONE_EVENT) {
                SearchServiceFactory.getService().setIndexInterval(searchAdminForm.getIndexInterval());
                return;
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
