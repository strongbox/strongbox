package org.carlspring.strongbox.validation.users;

import org.carlspring.strongbox.controllers.users.support.PathPrivilege;
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

    private static final String KEY_PATTERN =
            "/storages/{storageId}/{repositoryId}/{path:" + CustomAntPathMatcher.TWO_STARS_ANALOGUE + "}";

    @Inject
    @Named("customAntPathMatcher")
    private CustomAntPathMatcher antPathMatcher;

    @Override
    public void initialize(ValidAccessModelPath constraintAnnotation)
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

        final Map<String, String> uriTemplateVariables = new HashMap<>();
        for (PathPrivilege authority : pathAuthorities)
        {
            String key = authority.getPath();
            boolean matches = antPathMatcher.doMatch(KEY_PATTERN, key, true, uriTemplateVariables);
            if (!matches)
            {
                return false;
            }
        }

        return true;
    }

}
