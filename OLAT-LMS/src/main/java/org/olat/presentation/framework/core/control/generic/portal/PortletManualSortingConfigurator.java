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
 * Copyright (c) since 1999 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control.generic.portal;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * its important to provide this controller a tableModel which follows this configuration: column-# | content | Condition 1 Title - 2 Date only 2 columns 2/3 Description
 * 3 Columns, but only 2 showed 4 Type 4 columns in model
 */
public class PortletManualSortingConfigurator extends BasicController {

    private static final String ACTION_MULTISELECT_CHOOSE = "msc";
    private static final String ACTION_MULTISELECT_CANCEL = "cancel";

    private Panel tablePanel;
    private VelocityContainer mainVC;
    private TableController tableController;
    private PortletDefaultTableDataModel tableDataModel;

    private List<PortletEntry> sortedItems = new ArrayList<PortletEntry>();

    /**
     * @param ureq
     * @param wControl
     * @param fallBackTranslator
     * @param tableDataModel
     *            => See class-description!
     * @param sortedItems
     */
    public PortletManualSortingConfigurator(UserRequest ureq, WindowControl wControl, Translator fallBackTranslator, PortletDefaultTableDataModel tableDataModel,
            List<PortletEntry> sortedItems) {
        super(ureq, wControl, fallBackTranslator);
        this.tableDataModel = tableDataModel;
        this.sortedItems = sortedItems; // select the items in table!!!

        mainVC = this.createVelocityContainer("manualSorting");
        tablePanel = new Panel("table");
        mainVC.put("table", tablePanel);

        TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setMultiSelect(true);
        tableConfig.setSortingEnabled(true);
        tableConfig.setTableEmptyMessage("manual.sorting.no.entries.found");
        tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(tableController);
        int maxNumColumns = tableDataModel.getColumnCount();
        int columnCounter = 0;
        tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.title", columnCounter++, null, ureq.getLocale()));
        if (maxNumColumns == 2) {
            tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.date", columnCounter++, null, ureq.getLocale()));
        } else {
            tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.description", columnCounter++, null, ureq.getLocale()));
        }
        if (maxNumColumns == 4) {
            tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.type", columnCounter++, null, ureq.getLocale()));
        }
        tableController.addMultiSelectAction("action.choose", ACTION_MULTISELECT_CHOOSE);
        tableController.addMultiSelectAction("action.cancel", ACTION_MULTISELECT_CANCEL);
        tableController.setTableDataModel(tableDataModel);
        tablePanel.setContent(tableController.getInitialComponent());

        this.putInitialPanel(mainVC);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        fireEvent(ureq, event);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
            TableMultiSelectEvent multiselectionEvent = (TableMultiSelectEvent) event;
            if (multiselectionEvent.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
                // sortedItems = tableDataModel.getObjects(multiselectionEvent.getSelection());
                sortedItems = tableController.getSelectedSortedObjects(multiselectionEvent.getSelection(), tableDataModel);
                if (sortedItems.size() == 0) {
                    this.showWarning("portlet.sorting.manual.empty_sel");
                } else {
                    fireEvent(ureq, event);
                }
            } else if (multiselectionEvent.getAction().equals(ACTION_MULTISELECT_CANCEL)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    public List<PortletEntry> getSortedItems() {
        return sortedItems;
    }

}
