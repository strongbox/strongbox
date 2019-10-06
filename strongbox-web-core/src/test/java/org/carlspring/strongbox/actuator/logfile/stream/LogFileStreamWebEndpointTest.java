package org.carlspring.strongbox.actuator.logfile.stream;

import org.carlspring.strongbox.net.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Przemyslaw Fusik
 */
class LogFileStreamWebEndpointTest
{

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
                                                                      .withUserConfiguration(
                                                                              LogFileStreamWebEndpointTestConfiguration.class)
                                                                      .withConfiguration(AutoConfigurations.of(
                                                                              JacksonAutoConfiguration.class,
                                                                              HttpMessageConvertersAutoConfiguration.class,
                                                                              DispatcherServletAutoConfiguration.class,
                                                                              WebMvcAutoConfiguration.class,
                                                                              EndpointAutoConfiguration.class,
                                                                              WebEndpointAutoConfiguration.class,
                                                                              ManagementContextAutoConfiguration.class,
                                                                              ServletManagementContextAutoConfiguration.class,
                                                                              LogFileWebEndpointAutoConfiguration.class,
                                                                              LogFileStreamWebEndpointAutoConfiguration.class
                                                                      ));

    @Test
    void logFileWebEndpointUsesConfiguredExternalFile()
            throws IOException
    {
        Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        File file = Files.createTempFile(null, "logfile").toFile();
        FileCopyUtils.copy("--TEST--".getBytes(), file);
        this.contextRunner.withPropertyValues("management.endpoints.web.exposure.include=logfilestream",
                                              "management.endpoints.web.exposure.include=logfile",
                                              "management.endpoint.logfile.external-file:" +
                                              file.getAbsolutePath()).run((context) -> {
            assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class);
            assertThat(context).hasSingleBean(LogFileWebEndpoint.class);
            LogFileWebEndpoint endpoint = context.getBean(LogFileWebEndpoint.class);
            Resource resource = endpoint.logFile();
            assertThat(resource).isNotNull();
            assertThat(contentOf(resource.getFile())).isEqualTo("--TEST--");
        });
    }

    @Test
    void logFileWebEndpointUsesConfiguredExternalFileAndEmitsAsync(TestInfo testInfo)
            throws IOException
    {
        String methodName = testInfo.getTestMethod().get().getName();

        Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        File file = Files.createTempFile(methodName, "logfile").toFile();
        FileUtils.writeStringToFile(file, methodName, Charset.forName("UTF-8"), true);

        this.contextRunner.withPropertyValues(
                "management.endpoints.web.base-path=/api/monitoring",
                "management.endpoints.web.exposure.include=logfile,logfilestream",
                "management.endpoint.logfile.external-file:" + file.getAbsolutePath())
                          .run(withMockMvc((mockMvc) -> {

                              MvcResult mvcResult = mockMvc.perform(get("/api/monitoring/logfilestream"))
                                                           .andExpect(request().asyncStarted())
                                                           .andDo(MockMvcResultHandlers.log())
                                                           .andReturn();

                              FileUtils.writeStringToFile(file, methodName + "\n", Charset.forName("UTF-8"), true);

                              mockMvc.perform(asyncDispatch(mvcResult))
                                     .andDo(MockMvcResultHandlers.log())
                                     .andExpect(status().isOk())
                                     .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_UTF8_VALUE));

                              String response = mvcResult.getResponse().getContentAsString();

                              assertThat(response).containsIgnoringCase("event:stream");
                              assertThat(response).containsIgnoringCase("data:" + methodName);
                          }));
    }

    private ContextConsumer<WebApplicationContext> withMockMvc(MockMvcConsumer mockMvc)
    {
        return (context) -> mockMvc.accept(MockMvcBuilders.webAppContextSetup(context).build());
    }

    @FunctionalInterface
    interface MockMvcConsumer
    {

        void accept(MockMvc mockMvc)
                throws Exception;

    }

    @Configuration
    static class LogFileStreamWebEndpointTestConfiguration
    {

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public SseEmitterAwareTailerListenerAdapter tailerListener(SseEmitter sseEmitter)
        {
            return new TestSseEmitterAwareTailerListenerAdapter(sseEmitter);
        }

    }

    static class TestSseEmitterAwareTailerListenerAdapter
            extends SseEmitterAwareTailerListenerAdapter
    {

        public TestSseEmitterAwareTailerListenerAdapter(final SseEmitter sseEmitter)
        {
            super(sseEmitter);
        }

        @Override
        public void handle(final String line)
        {
            super.handle(line);
            if (sseEmitter != null)
            {
                sseEmitter.complete();
            }
        }
    }
}