package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class ServerSettingsFormTestIT
        extends RestAssuredBaseTest
{

    private static final String INSTANCE_NAME_VALID = "new-instance";
    private static final String URL_VALID = "url";
    private static final int PORT_VALID = 1;
    private static final int PORT_MIN_INVALID = 0;
    private static final int PORT_MAX_INVALID = 65536;
    private static SmtpConfigurationForm smtpConfigurationForm;
    private static ProxyConfigurationForm proxyConfigurationForm;

    @Inject
    private Validator validator;


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

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
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testServerSettingsFormInvalidEmptyInstanceName()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(StringUtils.EMPTY);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
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
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A base URL must be specified.");
    }

    @ParameterizedTest
    @ValueSource(ints = { PORT_MIN_INVALID,
                          PORT_MAX_INVALID })
    void testProxyConfigurationFormInvalidPort(int port)
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(port);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf(
                "Port number must be an integer between 1 and 65535.");
    }

    @Test
    void testServerSettingsFormInvalidSmtpConfigurationForm()
    {
        // given
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm();
        serverSettingsForm.setInstanceName(INSTANCE_NAME_VALID);
        serverSettingsForm.setBaseUrl(URL_VALID);
        serverSettingsForm.setPort(PORT_VALID);
        smtpConfigurationForm.setPort(0);
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
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
        serverSettingsForm.setSmtpConfigurationForm(smtpConfigurationForm);
        proxyConfigurationForm.setHost(StringUtils.EMPTY);
        serverSettingsForm.setProxyConfigurationForm(proxyConfigurationForm);

        // when
        Set<ConstraintViolation<ServerSettingsForm>> violations = validator.validate(serverSettingsForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A host must be specified.");
    }
}
