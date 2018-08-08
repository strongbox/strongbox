package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.authorization.domain.Role;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

/**
 * @author Pablo Tirado
 */
public class UniqueRoleNameValidator
        implements ConstraintValidator<UniqueRoleName, String>
{

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    @Override
    public void initialize(UniqueRoleName constraint)
    {
        // empty by design
    }

    @Override
    public boolean isValid(String roleName,
                           ConstraintValidatorContext context)
    {
        RoleDto role = new RoleDto(roleName, "");
        return StringUtils.isEmpty(roleName) ||
               !authorizationConfigService.get().getRoles().contains(new Role(role));
    }

}
