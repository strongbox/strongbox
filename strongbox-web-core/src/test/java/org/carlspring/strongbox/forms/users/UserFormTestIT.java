package org.carlspring.strongbox.forms.users;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class UserFormTestIT
        extends RestAssuredBaseTest
{

    private static final String VALID_USERNAME = "new-user";
    private static final String EXISTING_USERNAME = "deployer";
    private static final String VALID_PASSWORD = "new-password";
    private static final String INVALID_PASSWORD = "newpass";

    @Inject
    private Validator validator;

    private static Stream<Arguments> constraintGroupProvider()
    {
        return Stream.of(
                Arguments.of(UserForm.NewUser.class),
                Arguments.of(UserForm.ExistingUser.class)
        );
    }

    private static Stream<Arguments> passwordProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, "Password field is required!"),
                Arguments.of(INVALID_PASSWORD, "Password has to be more than 8 characters!")
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
    @MethodSource("constraintGroupProvider")
    void testUserFormValid(Class<?> constraintGroup)
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(VALID_USERNAME);
        userForm.setPassword(VALID_PASSWORD);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, constraintGroup);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testUserFormNewUserInvalidEmptyUsername()
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(StringUtils.EMPTY);
        userForm.setPassword(VALID_PASSWORD);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, UserForm.NewUser.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("Username is required!");
    }

    @Test
    void testUserFormNewUserInvalidExistingUsername()
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(EXISTING_USERNAME);
        userForm.setPassword(VALID_PASSWORD);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, UserForm.NewUser.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("Username is already taken.");
    }


    @ParameterizedTest
    @MethodSource("passwordProvider")
    void testUserFormNewUserInvalidPasswordLength(String password,
                                                  String errorMessage)
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(VALID_USERNAME);
        userForm.setPassword(password);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, UserForm.NewUser.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }

    @Test
    void testUserFormExistingUserValidEmptyPassword()
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(VALID_USERNAME);
        userForm.setPassword(StringUtils.EMPTY);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, UserForm.ExistingUser.class);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testUserFormExistingUserInvalidPasswordLength()
    {
        // given
        UserForm userForm = new UserForm();
        userForm.setUsername(VALID_USERNAME);
        userForm.setPassword(INVALID_PASSWORD);

        // when
        Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, UserForm.ExistingUser.class);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("Password has to be more than 8 characters!");
    }
}
