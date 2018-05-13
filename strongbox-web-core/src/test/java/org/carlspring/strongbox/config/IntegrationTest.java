package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.app.StrongboxSpringBootApplication;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.rest.common.RestAssuredTestExecutionListener;
import org.carlspring.strongbox.storage.indexing.remote.MockedIndexResourceFetcher;
import org.carlspring.strongbox.storage.indexing.remote.ResourceFetcherFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.index.updater.ResourceFetcher;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
                            IntegrationTest.IntegrationTestsConfiguration.class,
                            TestingCoreConfig.class })
@WebAppConfiguration("classpath:")
@WithUserDetails("admin")
@ActiveProfiles("test")
@TestExecutionListeners(listeners = { RestAssuredTestExecutionListener.class,
                                      CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(ExecutionMode.SAME_THREAD)
public @interface IntegrationTest
{

    @Configuration
    class IntegrationTestsConfiguration
    {

        @Bean
        @Primary
        CronJobSchedulerService mockCronJobSchedulerService()
        {
            return Mockito.mock(CronJobSchedulerService.class);
        }

        @Bean
        @Primary
        ResourceFetcher mockedIndexResourceFetcher()
        {
            return new MockedIndexResourceFetcher();
        }

        @Bean
        @Primary
        ResourceFetcherFactory mockedResourceFetcherMockFactory(ResourceFetcher resourceFetcher)
        {
            final ResourceFetcherFactory resourceFetcherFactory = Mockito.mock(ResourceFetcherFactory.class);

            Mockito.when(resourceFetcherFactory.createIndexResourceFetcher(ArgumentMatchers.anyString(),
                                                                           ArgumentMatchers.any(CloseableHttpClient.class)))
                   .thenReturn(resourceFetcher);

            return resourceFetcherFactory;
        }
    }

}
