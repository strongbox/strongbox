package org.carlspring.strongbox.forms;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class PrivilegeFormTestIT
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
    void testPrivilegeFormValid()
    {
        // given
        PrivilegeForm privilegeForm = new PrivilegeForm();
        String privilegeName = Privileges.AUTHENTICATED_USER.name();
        privilegeForm.setName(privilegeName);

        // when
        Set<ConstraintViolation<PrivilegeForm>> violations = validator.validate(privilegeForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testPrivilegeFormInvalidEmptyName()
    {
        // given
        PrivilegeForm privilegeForm = new PrivilegeForm();
        String privilegeName = StringUtils.EMPTY;
        privilegeForm.setName(privilegeName);

        // when
        Set<ConstraintViolation<PrivilegeForm>> violations = validator.validate(privilegeForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A name must be specified.");
    }
}
