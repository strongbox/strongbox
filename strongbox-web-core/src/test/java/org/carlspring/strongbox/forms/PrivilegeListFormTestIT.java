package org.carlspring.strongbox.forms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class PrivilegeListFormTestIT
        extends RestAssuredBaseTest
{

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
    void testPrivilegeListFormValid()
    {
        // given
        List<Privileges> privileges = new ArrayList<>();
        privileges.add(Privileges.AUTHENTICATED_USER);
        privileges.add(Privileges.ADMIN);
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<PrivilegeListForm>> violations = validator.validate(privilegeListForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testPrivilegeListFormInvalidPrivilegeWithEmptyName()
    {
        // given
        List<Privileges> privileges = new ArrayList<>();
        privileges.add(null);
        privileges.add(Privileges.ADMIN);
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<PrivilegeListForm>> violations = validator.validate(privilegeListForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("messageTemplate").containsAnyOf("{javax.validation.constraints.NotNull.message}");
    }
}
