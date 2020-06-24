package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm.ProxyConfigurationFormChecks;
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
public class ProxyConfigurationFormTestIT
        extends RestAssuredBaseTest
{

    private static final String HOST_VALID = "host";
    private static final Integer PORT_VALID = 1;
    private static final Integer PORT_EMPTY = null;
    private static final Integer PORT_MIN_INVALID = 0;
    private static final Integer PORT_MAX_INVALID = 65536;
    private static final String TYPE_VALID = "DIRECT";
    private static final String TYPE_INVALID = "TYPE_INVALID";

    @Inject
    private Validator validator;

    private static Stream<Arguments> portsProvider()
    {
        final String rangeErrorMessage = "The port number must be an integer between 1 and 65535.";
        return Stream.of(
                Arguments.of(PORT_EMPTY, "A port must be provided."),
                Arguments.of(PORT_MIN_INVALID, rangeErrorMessage),
                Arguments.of(PORT_MAX_INVALID, rangeErrorMessage)
        );
    }

    private static Stream<Arguments> typesProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A proxy type must be specified."),
                Arguments.of(TYPE_INVALID, 1,
                             "The proxy type must contain one the following strings as value: DIRECT, HTTP, SOCKS4, SOCKS5")
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
    @ValueSource(strings = { "DIRECT",
                             "http",
                             "SOCKS4",
                             "socks5" })
    void testProxyConfigurationFormValid(String validType)
    {
        // given
        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost(HOST_VALID);
        proxyConfigurationForm.setPort(PORT_VALID);
        proxyConfigurationForm.setType(validType);

        // when
        Set<ConstraintViolation<ProxyConfigurationForm>> violations = validator.validate(proxyConfigurationForm,
                                                                                         ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testProxyConfigurationFormInvalidEmptyHost()
    {
        // given
        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost(StringUtils.EMPTY);
        proxyConfigurationForm.setPort(PORT_VALID);
        proxyConfigurationForm.setType(TYPE_VALID);

        // when
        Set<ConstraintViolation<ProxyConfigurationForm>> violations = validator.validate(proxyConfigurationForm,
                                                                                         ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A host must be specified.");
    }

    @ParameterizedTest
    @MethodSource("portsProvider")
    void testProxyConfigurationFormInvalidPort(Integer port,
                                               String errorMessage)
    {
        // given
        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost(HOST_VALID);
        proxyConfigurationForm.setPort(port);
        proxyConfigurationForm.setType(TYPE_VALID);

        // when
        Set<ConstraintViolation<ProxyConfigurationForm>> violations = validator.validate(proxyConfigurationForm,
                                                                                         ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }

    @ParameterizedTest
    @MethodSource("typesProvider")
    void testProxyConfigurationFormInvalidType(String type,
                                               int numErrors,
                                               String errorMessage)
    {
        // given
        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost(HOST_VALID);
        proxyConfigurationForm.setPort(PORT_VALID);
        proxyConfigurationForm.setType(type);

        // when
        Set<ConstraintViolation<ProxyConfigurationForm>> violations = validator.validate(proxyConfigurationForm,
                                                                                         ProxyConfigurationFormChecks.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(numErrors);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }
}
