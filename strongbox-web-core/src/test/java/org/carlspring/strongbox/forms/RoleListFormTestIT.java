package org.carlspring.strongbox.forms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RoleListFormTestIT
        extends RestAssuredBaseTest
{

    private final String NEW_ROLE = "NEW_ROLE";
    private final String NEW_ROLE_2 = "NEW_ROLE_2";
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
    void testRoleListFormValid()
    {
        // given
        RoleForm role1 = new RoleForm();
        role1.setName(NEW_ROLE);

        RoleForm role2 = new RoleForm();
        role2.setName(NEW_ROLE_2);

        List<RoleForm> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        RoleListForm roleListForm = new RoleListForm();
        roleListForm.setRoles(roles);

        // when
        Set<ConstraintViolation<RoleListForm>> violations = validator.validate(roleListForm);

        // then
        assertThat(violations)
                .as("Violations are not empty!")
                .isEmpty();
    }

    @Test
    void testRoleListFormInvalidRoleWithEmptyName()
    {
        // given
        RoleForm role1 = new RoleForm();
        role1.setName(StringUtils.EMPTY);

        RoleForm role2 = new RoleForm();
        role2.setName(NEW_ROLE);

        List<RoleForm> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        RoleListForm roleListForm = new RoleListForm();
        roleListForm.setRoles(roles);

        // when
        Set<ConstraintViolation<RoleListForm>> violations = validator.validate(roleListForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A name must be specified.");
    }

    @Test
    void testRoleListFormInvalidAlreadyRegisteredName()
    {
        // given
        RoleForm role1 = new RoleForm();
        role1.setName(EXISTING_ROLE);

        RoleForm role2 = new RoleForm();
        role2.setName(NEW_ROLE);

        List<RoleForm> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        RoleListForm roleListForm = new RoleListForm();
        roleListForm.setRoles(roles);

        // when
        Set<ConstraintViolation<RoleListForm>> violations = validator.validate(roleListForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("Role is already registered.");
    }
}
