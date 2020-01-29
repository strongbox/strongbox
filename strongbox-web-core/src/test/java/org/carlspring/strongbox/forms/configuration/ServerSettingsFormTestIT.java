package org.carlspring.strongbox.forms.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm.ProxyConfigurationFormChecks;
import org.carlspring.strongbox.forms.configuration.SmtpConfigurationForm.SmtpConfigurationFormChecks;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class ServerSettingsFormTestIT
        extends RestAssuredBaseTest
{

    private static final String INSTANCE_NAME_VALID = "new-instance";
    private static final String URL_VALID = "url";
    private static final Integer PORT_VALID = 1;
    private static final Integer PORT_EMPTY = null;
    private static final Integer PORT_MIN_INVALID = 0;
    private static final Integer PORT_MAX_INVALID = 65536;
    private static CorsConfigurationForm corsConfigurationForm;
    private static SmtpConfigurationForm smtpConfigurationForm;
    private static ProxyConfigurationForm proxyConfigurationForm;

    @Inject
    private Validator validator;

    private static Stream<Arguments> portsProvider()
    {
        final String rangeErrorMessage = "Port number must be an integer between 1 and 65535.";
        return Stream.of(
                Arguments.of(PORT_EMPTY, "A port must be specified."),
                Arguments.of(PORT_MIN_INVALID, rangeErrorMessage),
                Arguments.of(PORT_MAX_INVALID, rangeErrorMessage)
        );
    }


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        corsConfigurationForm = new CorsConfigurationForm();
        List<String> allowedOrigins = Lists.newArrayList("http://example-a.com/");
        corsConfigurationForm.setAllowedOrigins(allowedOrigins);

        smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost("host");
        smtpConfigurationForm.setPort(1);
        smtpConfigurationForm.setConnection("ssl");

        proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost("host");
        proxyConfigurationForm.setPort(1);
        proxyConfigurationForm.setType("DIRECT");
    }

    @Test
    void testServerSettingsFormValid()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testServerSettingsFormValidWhenChildrenFormsEmpty()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);

        corsConfigurationForm = new CorsConfigurationForm();
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);

        smtpConfigurationForm = new SmtpConfigurationForm();
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);

        proxyConfigurationForm = new ProxyConfigurationForm();
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm, Default.class);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testServerSettingsFormInvalidEmptyInstanceName()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(StringUtils.EMPTY);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("The name of this instance");
    }

    @Test
    void testServerSettingsFormInvalidEmptyUrl()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(StringUtils.EMPTY);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A base URL must be specified.");
    }

    @ParameterizedTest
    @MethodSource("portsProvider")
    void testServerSettingsFormInvalidPort(Integer port,
                                           String errorMessage)
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(port);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }

    @Test
    void testServerSettingsFormInvalidSmtpConfigurationForm()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        smtpConfigurationForm.setPort(0);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(
                "Port number must be an integer between 1 and 65535.");
    }

    @Test
    void testServerSettingsFormInvalidProxyConfigurationForm()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setCorsConfigurationForm(corsConfigurationForm);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        proxyConfigurationForm.setHost(StringUtils.EMPTY);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm,
                                                                                     Default.class,
                                                                                     SmtpConfigurationFormChecks.class,
                                                                                     ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A host must be specified.");
    }
}
