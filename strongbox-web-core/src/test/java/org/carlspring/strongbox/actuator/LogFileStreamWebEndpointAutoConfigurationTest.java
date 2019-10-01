package org.carlspring.strongbox.actuator;


import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 * @see <a href="https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-actuator-autoconfigure/src/test/java/org/springframework/boot/actuate/autoconfigure/logging/LogFileWebEndpointAutoConfigurationTests.java'>LogFileWebEndpointAutoConfigurationTests</a>
 */
class LogFileStreamWebEndpointAutoConfigurationTest
{

    private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
                                                                .withConfiguration(
                                                                        AutoConfigurations.of(
                                                                                LogFileStreamWebEndpointAutoConfiguration.class));

    @Test
    void runWithOnlyExposedShouldNotHaveEndpointBean()
    {
        this.contextRunner.withPropertyValues("management.endpoints.web.exposure.include=logfilestream")
                          .run((context) -> assertThat(context).doesNotHaveBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void runWhenLoggingFileIsSetAndNotExposedShouldHaveEndpointBean()
    {
        this.contextRunner.withPropertyValues("logging.file:test.log")
                          .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void runWhenLoggingFileIsSetAndExposedShouldHaveEndpointBean()
    {
        this.contextRunner
                .withPropertyValues("logging.file:test.log", "management.endpoints.web.exposure.include=logfilestream")
                .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    @Deprecated
    void runWhenLoggingFileIsSetWithDeprecatedPropertyAndExposedShouldHaveEndpointBean()
    {
        this.contextRunner
                .withPropertyValues("logging.file:test.log", "management.endpoints.web.exposure.include=logfilestream")
                .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void runWhenLoggingPathIsSetAndNotExposedShouldHaveEndpointBean()
    {
        this.contextRunner.withPropertyValues("logging.path:test/logs")
                          .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void runWhenLoggingPathIsSetAndExposedShouldHaveEndpointBean()
    {
        this.contextRunner
                .withPropertyValues("logging.path:test/logs", "management.endpoints.web.exposure.include=logfilestream")
                .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    @Deprecated
    void runWhenLoggingPathIsSetWithDeprecatedPropertyAndExposedShouldHaveEndpointBean()
    {
        this.contextRunner
                .withPropertyValues("logging.path:test/logs", "management.endpoints.web.exposure.include=logfilestream")
                .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void logFileWebEndpointIsAutoConfiguredWhenExternalFileIsSet()
    {
        this.contextRunner
                .withPropertyValues("management.endpoint.logfile.external-file:external.log",
                                    "management.endpoints.web.exposure.include=logfilestream")
                .run((context) -> assertThat(context).hasSingleBean(LogFileStreamWebEndpoint.class));
    }

    @Test
    void logFileWebEndpointCanBeDisabled()
    {
        this.contextRunner.withPropertyValues("logging.file:test.log",
                                              "management.endpoint.logfilestream.enabled:false")
                          .run((context) -> assertThat(context).doesNotHaveBean(LogFileStreamWebEndpoint.class));
    }

}