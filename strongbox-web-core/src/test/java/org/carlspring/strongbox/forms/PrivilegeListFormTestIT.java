package org.carlspring.strongbox.forms;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(violations.isEmpty()).as("Violations are not empty!").isTrue();
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
        assertThat(violations.isEmpty()).as("Violations are empty!").isFalse();
        assertThat(violations.size()).isEqualTo(1);
        assertThat(violations).extracting("messageTemplate").containsAnyOf("{javax.validation.constraints.NotNull.message}");
    }
}
