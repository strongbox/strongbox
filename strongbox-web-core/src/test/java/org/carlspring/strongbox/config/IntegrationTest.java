package org.carlspring.strongbox.config;

import org.carlspring.strongbox.MockedRemoteRepositoriesHeartbeatConfig;
import org.carlspring.strongbox.app.StrongboxSpringBootApplication;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.rest.client.MockMvcRequestSpecificationProxyTarget;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.storage.indexing.remote.MockedIndexResourceFetcher;
import org.carlspring.strongbox.storage.indexing.remote.ResourceFetcherFactory;
import org.carlspring.strongbox.testing.MavenMetadataServiceHelper;

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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import io.restassured.module.mockmvc.internal.MockMvcFactory;
import io.restassured.module.mockmvc.internal.MockMvcRequestSpecificationImpl;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;


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
@TestExecutionListeners(listeners = CacheManagerTestExecutionListener.class,
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
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

        @Bean
        public MavenMetadataServiceHelper mavenMetadataServiceHelper()
        {
            return new MavenMetadataServiceHelper();
        }
        
        @Bean
        @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public MockMvcRequestSpecification mockMvc(ApplicationContext applicationContext) {
            
            DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext);

            ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
            ObjectMapperConfig objectMapperFactory = new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass,
                                                                                                           s) -> objectMapper);
            RestAssuredMockMvcConfig config = RestAssuredMockMvcConfig.config().objectMapperConfig(objectMapperFactory);

            return new MockMvcRequestSpecificationProxyTarget(
                    new MockMvcRequestSpecificationImpl(new MockMvcFactory(builder), config, null, null,
                            null, null, null, null));
        }

        @Bean
        public RestAssuredArtifactClient artifactClient(MockMvcRequestSpecification mockMvc) 
        {
            return new RestAssuredArtifactClient(mockMvc);
        }

    }

}
