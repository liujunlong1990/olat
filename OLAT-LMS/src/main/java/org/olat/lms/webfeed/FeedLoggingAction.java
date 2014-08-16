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

package org.olat.lms.webfeed;

import java.lang.reflect.Field;

import org.olat.lms.activitylogging.ActionObject;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.activitylogging.ActionVerb;
import org.olat.lms.activitylogging.BaseLoggingAction;
import org.olat.lms.activitylogging.CrudAction;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ResourceableTypeList;

/**
 * LoggingActions used by feeds (blog / podcasts)
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <p>
 * Especially when modifying the ResourceableTypeList - which is a exercise where we try to predict/document/define which ResourceableTypes will later on - at runtime -
 * be available to the IUserActivityLogger.log() method.
 * <p>
 * The names of the LoggingAction should be self-describing.
 * <p>
 * Initial Date: 03.12.2009 <br>
 * 
 * @author Florian Gngi
 */
public class FeedLoggingAction extends BaseLoggingAction {

    private static ResourceableTypeList FEED_READ_RESOURCES = new ResourceableTypeList()
            .
            // this one is a message in a feed-node in a course
            addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.feed, OlatResourceableType.feedItem).or()
            .addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.feedItem).
            // this one is a message in a standalone feed
            or().addMandatory(OlatResourceableType.feed, OlatResourceableType.feedItem);

    /**
     * Feed level actions
     */

    public static final ILoggingAction FEED_READ = new FeedLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.resource)
            .setTypeList(FEED_READ_RESOURCES);

    public static final ILoggingAction FEED_EDIT = new FeedLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.edit, ActionObject.resource)
            .setTypeList(FEED_READ_RESOURCES);

    /**
     * Item level actions
     */

    public static final ILoggingAction FEED_ITEM_CREATE = new FeedLoggingAction(ActionType.tracking, CrudAction.create, ActionVerb.add, ActionObject.feeditem)
            .setTypeList(FEED_READ_RESOURCES);

    public static final ILoggingAction FEED_ITEM_READ = new FeedLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.view, ActionObject.feeditem)
            .setTypeList(FEED_READ_RESOURCES);

    public static final ILoggingAction FEED_ITEM_EDIT = new FeedLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.edit, ActionObject.feeditem)
            .setTypeList(FEED_READ_RESOURCES);

    public static final ILoggingAction FEED_ITEM_DELETE = new FeedLoggingAction(ActionType.tracking, CrudAction.delete, ActionVerb.remove, ActionObject.feeditem)
            .setTypeList(FEED_READ_RESOURCES);

    /**
     * This static constructor's only use is to set the javaFieldIdForDebug on all of the LoggingActions defined in this class.
     * <p>
     * This is used to simplify debugging - as it allows to issue (technical) log statements where the name of the LoggingAction Field is written.
     */
    static {
        final Field[] fields = FeedLoggingAction.class.getDeclaredFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                if (field.getType() == FeedLoggingAction.class) {
                    try {
                        final FeedLoggingAction aLoggingAction = (FeedLoggingAction) field.get(null);
                        aLoggingAction.setJavaFieldIdForDebug(field.getName());
                    } catch (final IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Simple wrapper calling super<init>
     * 
     */
    FeedLoggingAction(final ActionType resourceActionType, final CrudAction action, final ActionVerb actionVerb, final ActionObject actionObject) {
        super(resourceActionType, action, actionVerb, actionObject.name());
    }

}
