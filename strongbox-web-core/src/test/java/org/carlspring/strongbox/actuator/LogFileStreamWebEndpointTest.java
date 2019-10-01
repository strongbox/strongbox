package org.carlspring.strongbox.actuator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Przemyslaw Fusik
 */
class LogFileStreamWebEndpointTest
{

    private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
                                                                .withConfiguration(
                                                                        AutoConfigurations.of(
                                                                                LogFileWebEndpointAutoConfiguration.class,
                                                                                LogFileStreamWebEndpointAutoConfiguration.class,
                                                                                WebMvcAutoConfiguration.class));

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
    void logFileWebEndpointUsesConfiguredExternalFileAndEmitsAsync()
            throws IOException
    {
        Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        File file = Files.createTempFile(null, "logfile").toFile();
        FileCopyUtils.copy("--TEST--".getBytes(), file);

        WebApplicationContextRunner run = this.contextRunner.withPropertyValues(
                "management.endpoints.web.base-path=/api/monitoring",
                "management.endpoints.web.exposure.include=logfile,logfilestream",
                "management.endpoint.logfile.external-file:" +
                file.getAbsolutePath()).run(withMockMvc((mockMvc) -> {
            MvcResult mvcResult = mockMvc.perform(get("/api/monitoring/logfile"))
                                         .andExpect(status().isOk())
                                         //.andExpect(request().asyncStarted())
                                         .andDo(MockMvcResultHandlers.log())
                                         .andReturn();
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
}