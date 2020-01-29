package org.carlspring.strongbox.forms.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.config.IntegrationTest;
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
public class RemoteRepositoryFormTestIT
        extends RestAssuredBaseTest
{

    private static final String URL_VALID = "url";
    private static final Integer CHECK_INTERVAL_SECONDS_VALID = 1;
    private static final Integer CHECK_INTERVAL_SECONDS_INVALID = -1;

    @Inject
    private Validator validator;

    private static Stream<Arguments> checkIntervalSecondsProvider()
    {
        return Stream.of(
                Arguments.of(null, "A checkIntervalSeconds must be specified."),
                Arguments.of(CHECK_INTERVAL_SECONDS_INVALID,
                             "A checkIntervalSeconds must be positive or zero.")
        );
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    void testRemoteRepositoryFormValid()
    {
        // given
        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(URL_VALID);
        remoteRepositoryForm.setCheckIntervalSeconds(CHECK_INTERVAL_SECONDS_VALID);

        // when
        Set<ConstraintViolation<RemoteRepositoryForm>> violations = validator.validate(remoteRepositoryForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRemoteRepositoryFormInvalidEmptyUrl()
    {
        // given
        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(StringUtils.EMPTY);
        remoteRepositoryForm.setCheckIntervalSeconds(CHECK_INTERVAL_SECONDS_VALID);

        // when
        Set<ConstraintViolation<RemoteRepositoryForm>> violations = validator.validate(remoteRepositoryForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("An url must be specified.");
    }

    @ParameterizedTest
    @MethodSource("checkIntervalSecondsProvider")
    void testRemoteRepositoryFormInvalidIntervalSeconds(Integer checkIntervalSeconds,
                                                        String errorMessage)
    {
        // given
        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(URL_VALID);
        remoteRepositoryForm.setCheckIntervalSeconds(checkIntervalSeconds);

        // when
        Set<ConstraintViolation<RemoteRepositoryForm>> violations = validator.validate(remoteRepositoryForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }
}
