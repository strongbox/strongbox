package org.carlspring.strongbox.cron.context;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.config.*;
import org.carlspring.strongbox.cron.config.CronTasksConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { CommonConfig.class,
                                  StorageApiConfig.class,
                                  MockedRemoteRepositoriesHeartbeatConfig.class,
                                  TestingCoreConfig.class,
                                  ClientConfig.class,
                                  EventsConfig.class,
                                  CronTasksConfig.class })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface CronTaskTest
{

}
