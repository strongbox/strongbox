package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.SmtpConfigurationForm.SmtpConfigurationFormChecks;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class SmtpConfigurationFormTestIT
        extends RestAssuredBaseTest
{

    private static final String HOST_VALID = "host";
    private static final Integer PORT_VALID = 1;
    private static final Integer PORT_EMPTY = null;
    private static final Integer PORT_MIN_INVALID = 0;
    private static final Integer PORT_MAX_INVALID = 65536;
    private static final String CONNECTION_VALID = "plain";
    private static final String CONNECTION_INVALID = "CONNECTION_INVALID";

    @Inject
    private Validator validator;

    private static Stream<Arguments> connectionProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, "Please set a valid SMTP connection type."),
                Arguments.of(CONNECTION_INVALID, "Please set a valid SMTP connection type.")
        );
    }

    private static Stream<Arguments> portsProvider()
    {
        final String rangeErrorMessage = "Port number must be an integer between 1 and 65535.";
        return Stream.of(
                Arguments.of(PORT_EMPTY, "SMTP port must be provided."),
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
    }

    @ParameterizedTest
    @ValueSource(strings = { "plain",
                             "SSL",
                             "tls" })
    void testSmtpConfigurationFormValid(String connection)
    {
        // given
        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost(HOST_VALID);
        smtpConfigurationForm.setPort(PORT_VALID);
        smtpConfigurationForm.setConnection(connection);

        // when
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm,
                                                                                        SmtpConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testSmtpConfigurationFormInvalidEmptyHost()
    {
        // given
        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost(StringUtils.EMPTY);
        smtpConfigurationForm.setPort(PORT_VALID);
        smtpConfigurationForm.setConnection(CONNECTION_VALID);

        // when
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm,
                                                                                        SmtpConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("SMTP host must be provided.");
    }

    @ParameterizedTest
    @MethodSource("portsProvider")
    void testSmtpConfigurationFormInvalidPort(Integer port,
                                              String errorMessage)
    {
        // given
        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost(HOST_VALID);
        smtpConfigurationForm.setPort(port);
        smtpConfigurationForm.setConnection(CONNECTION_VALID);

        // when
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm,
                                                                                        SmtpConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }

    @ParameterizedTest
    @MethodSource("connectionProvider")
    void testSmtpConfigurationFormInvalidConnection(String connection,
                                                    String errorMessage)
    {
        // given
        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost(HOST_VALID);
        smtpConfigurationForm.setPort(PORT_VALID);
        smtpConfigurationForm.setConnection(connection);

        // when
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm,
                                                                                        SmtpConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }
}
