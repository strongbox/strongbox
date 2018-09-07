package org.carlspring.strongbox.validation.users;

import org.carlspring.strongbox.forms.users.PathPrivilege;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

/**
 * @author Pablo Tirado
 */
public class ValidAccessModelPathValidator
        implements ConstraintValidator<ValidAccessModelPath, List<PathPrivilege>>
{

    @Inject
    @Named("customAntPathMatcher")
    private CustomAntPathMatcher antPathMatcher;

    private ValidAccessModelPath constraint;

    @Override
    public void initialize(ValidAccessModelPath constraintAnnotation)
    {
        this.constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(List<PathPrivilege> pathAuthorities,
                           ConstraintValidatorContext context)
    {
        if (pathAuthorities == null || CollectionUtils.isEmpty(pathAuthorities))
        {
            return true;
        }

        final Map<String, String> uriTemplateVariables = new HashMap<>();
        for (PathPrivilege authority : pathAuthorities)
        {
            String key = authority.getPath();
            boolean matches = antPathMatcher.doMatch(constraint.regexp(), key, true, uriTemplateVariables);
            if (!matches)
            {
                return false;
            }
        }

        return true;
    }

}
