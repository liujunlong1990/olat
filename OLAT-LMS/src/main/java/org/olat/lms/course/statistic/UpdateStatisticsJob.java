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
package org.olat.lms.course.statistic;

import org.apache.log4j.Logger;
import org.olat.lms.commons.scheduler.JobWithDB;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * job that updates all the statistics wrapped in DBjob to close DB sessions properly
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class UpdateStatisticsJob extends JobWithDB {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */
    @Override
    public void executeWithDB(final JobExecutionContext arg0) throws JobExecutionException {
        final StatisticUpdateService statisticManager = (StatisticUpdateService) CoreSpringFactory.getBean(StatisticUpdateService.class);
        if (statisticManager == null) {
            log.error("executeWithDB: UpdateStatisticsJob configured, but no StatisticUpdateManager available");
        } else {
            if (!statisticManager.updateStatistics(false, null)) {
                log.warn("executeWithDB: UpdateStatisticsJob could not trigger updateStatistics - must be already running");
            }
        }
    }

}
