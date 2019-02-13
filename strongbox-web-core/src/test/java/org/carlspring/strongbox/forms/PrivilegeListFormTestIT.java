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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@SpringBootTest
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
        PrivilegeForm authenticatedUserPrivilege = new PrivilegeForm();
        String privilegeName = Privileges.AUTHENTICATED_USER.name();
        authenticatedUserPrivilege.setName(privilegeName);

        PrivilegeForm adminUserPrivilege = new PrivilegeForm();
        privilegeName = Privileges.ADMIN.name();
        adminUserPrivilege.setName(privilegeName);

        List<PrivilegeForm> privileges = new ArrayList<>();
        privileges.add(authenticatedUserPrivilege);
        privileges.add(adminUserPrivilege);
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<PrivilegeListForm>> violations = validator.validate(privilegeListForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testPrivilegeListFormInvalidPrivilegeWithEmptyName()
    {
        // given
        PrivilegeForm emptyPrivilege = new PrivilegeForm();
        String privilegeName = StringUtils.EMPTY;
        emptyPrivilege.setName(privilegeName);

        PrivilegeForm adminUserPrivilege = new PrivilegeForm();
        privilegeName = Privileges.ADMIN.name();
        adminUserPrivilege.setName(privilegeName);

        List<PrivilegeForm> privileges = new ArrayList<>();
        privileges.add(emptyPrivilege);
        privileges.add(adminUserPrivilege);
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<PrivilegeListForm>> violations = validator.validate(privilegeListForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A name must be specified.");
    }
}
