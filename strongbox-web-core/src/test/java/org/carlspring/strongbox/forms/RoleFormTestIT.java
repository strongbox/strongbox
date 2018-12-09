package org.carlspring.strongbox.forms;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RoleFormTestIT
        extends RestAssuredBaseTest
{

    private final String NEW_ROLE = "NEW_ROLE";
    private final String EXISTING_ROLE = "CUSTOM_ROLE";
    @Inject
    private Validator validator;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    void testRoleFormValid()
    {
        // given
        RoleForm role = new RoleForm();
        role.setName(NEW_ROLE);

        // when
        Set<ConstraintViolation<RoleForm>> violations = validator.validate(role);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRoleFormInvalidEmptyName()
    {
        // given
        RoleForm role = new RoleForm();
        role.setName(StringUtils.EMPTY);

        // when
        Set<ConstraintViolation<RoleForm>> violations = validator.validate(role);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A name must be specified.");
    }

    @Test
    void testRoleFormInvalidAlreadyRegisteredName()
    {
        // given
        RoleForm role = new RoleForm();
        role.setName(EXISTING_ROLE);

        // when
        Set<ConstraintViolation<RoleForm>> violations = validator.validate(role);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("Role is already registered.");
    }
}
