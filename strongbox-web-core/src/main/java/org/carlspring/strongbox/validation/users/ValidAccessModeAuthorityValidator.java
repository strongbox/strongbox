package org.carlspring.strongbox.validation.users;

import org.carlspring.strongbox.controllers.users.support.PathPrivilege;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * @author Pablo Tirado
 */
public class ValidAccessModeAuthorityValidator
        implements ConstraintValidator<ValidAccessModelPrivilege, List<PathPrivilege>>
{

    @Override
    public void initialize(ValidAccessModelPrivilege constraintAnnotation)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(List<PathPrivilege> pathAuthorities,
                           ConstraintValidatorContext context)
    {
        if (pathAuthorities == null || CollectionUtils.isEmpty(pathAuthorities))
        {
            return true;
        }

        for (PathPrivilege authority : pathAuthorities)
        {
            if (CollectionUtils.isEmpty(authority.getPrivileges()))
            {
                return false;
            }
        }

        return true;
    }

}
