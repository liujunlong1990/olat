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
package org.olat.lms.core.notification.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationTestDataGenerator;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Initial Date: 11.04.2012 <br>
 * 
 * @author Branislav Balaz
 */
// TODO: REVIEW PERFORMANCE TESTS FOR PUBLISH bb/12.04.2012
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class PublishEventPerformance extends AbstractJUnit4SpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private NotificationService notificationServiceImpl;
    @Autowired
    private NotificationTestDataGenerator notificationTestDataGenerator;
    @Autowired
    private HibernateTransactionManager transactionManager;

    private Publisher publisher;
    private Identity creatorIdentity;

    private final int NUMBER_OF_SUBSCRIBERS = 500;
    private final int NUMBER_OF_PUBLISHERS = 1;
    private final int NUMBER_OF_CREATOR_IDENTITIES = 1;
    private final long MAX_EXECUTION_TIME_IN_SECONDS = 3;

    @Before
    public void setup() {
        log.info("setup started");

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<Subscriber> subscribers = notificationTestDataGenerator.generateSubscribers(NUMBER_OF_SUBSCRIBERS);
                publisher = notificationTestDataGenerator.generatePublishers(NUMBER_OF_PUBLISHERS).get(0);
                notificationTestDataGenerator.generateSubscriptionsForListSubscribersAndOnePublisher(subscribers, publisher);
                creatorIdentity = notificationTestDataGenerator.generateIdentities(NUMBER_OF_CREATOR_IDENTITIES).get(0);
                assertEquals(NUMBER_OF_SUBSCRIBERS, notificationServiceImpl.getAllSubscriberKeys().size());
            }
        });
        log.info("setup finished");
    }

    @Test
    public void publishEventManySubscriptionsForOnePublisher() {
        long startTime = System.currentTimeMillis();
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                int numEvents = notificationServiceImpl.publishEvent(getPublishEventTO());
                log.info("publishEventManySubscriptionsForOnePublisher - numEvents: " + numEvents);
                assertEquals(NUMBER_OF_SUBSCRIBERS * NUMBER_OF_PUBLISHERS, numEvents);
            }
        });
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        log.info("DURATION in seconds: " + duration);
        assertTrue("maximal execution time for " + NUMBER_OF_SUBSCRIBERS + " subscriptions exceeded: maximal time in seconds: " + MAX_EXECUTION_TIME_IN_SECONDS
                + ", actual duration in seconds: " + duration, duration <= MAX_EXECUTION_TIME_IN_SECONDS);

    }

    private PublishEventTO getPublishEventTO() {
        return PublishEventTO.getValidInstance(publisher.getContextType(), publisher.getContextId(), "", publisher.getSubcontextId(), publisher.getSourceType(),
                publisher.getSourceId(), "", "", creatorIdentity, PublishEventTO.EventType.NEW);
    }

    @After
    public void cleanUp() {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                notificationTestDataGenerator.cleanupNotificationTestData();
            }
        });
    }

}
