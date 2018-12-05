package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class SmtpConfigurationFormTestIT
        extends RestAssuredBaseTest
{

    private static final String HOST_VALID = "host";
    private static final int PORT_VALID = 1;
    private static final int PORT_MIN_INVALID = 0;
    private static final int PORT_MAX_INVALID = 65536;
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
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
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
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("SMTP host must be provided");
    }

    @ParameterizedTest
    @ValueSource(ints = { PORT_MIN_INVALID,
                          PORT_MAX_INVALID })
    void testSmtpConfigurationFormInvalidPort(int invalidPort)
    {
        // given
        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm();
        smtpConfigurationForm.setHost(HOST_VALID);
        smtpConfigurationForm.setPort(invalidPort);
        smtpConfigurationForm.setConnection(CONNECTION_VALID);

        // when
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf(
                "Port number must be an integer between 1 and 65535.");
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
        Set<ConstraintViolation<SmtpConfigurationForm>> violations = validator.validate(smtpConfigurationForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }
}
