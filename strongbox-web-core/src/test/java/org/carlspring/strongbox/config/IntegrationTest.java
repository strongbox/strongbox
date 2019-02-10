package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.app.StrongboxSpringBootApplication;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.rest.common.RestAssuredTestExecutionListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;


/**
 * Helper meta annotation for all rest-assured based tests. Specifies tests that
 * require web server and remote HTTP protocol.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { StrongboxSpringBootApplication.class,
                            MockedRemoteRepositoriesHeartbeatConfig.class,
                            RestAssuredConfig.class })
@WebAppConfiguration("classpath:")
@WithUserDetails(value = "admin")
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { RestAssuredTestExecutionListener.class,
                                      CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(ExecutionMode.SAME_THREAD)
public @interface IntegrationTest
{

}
