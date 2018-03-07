package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

import org.springframework.util.StringUtils;

/**
 * @author Pablo Tirado
 */
public class UniqueRoleNameValidator
        implements ConstraintValidator<UniqueRoleName, String>
{

    private AuthorizationConfigProvider configProvider;

    private AuthorizationConfig config;

    public UniqueRoleNameValidator(AuthorizationConfigProvider configProvider)
    {
        this.configProvider = configProvider;
    }

    @Override
    public void initialize(UniqueRoleName constraint)
    {
        Optional<AuthorizationConfig> configOptional = configProvider.get();
        configOptional.ifPresent(authorizationConfig -> config = authorizationConfig);
    }

    @Override
    public boolean isValid(String roleName,
                           ConstraintValidatorContext context)
    {
        Role role = new Role(roleName, "");
        return StringUtils.isEmpty(roleName) || !config.getRoles().getRoles().contains(role);
    }

}
