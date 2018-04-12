package org.carlspring.strongbox.validation.users;

import org.carlspring.strongbox.utils.CustomAntPathMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

/**
 * @author Pablo Tirado
 */
public class ValidAccessModelMapKeyValidator
        implements ConstraintValidator<ValidAccessModelMapKey, Map<String, Collection<String>>>
{

    private static final String KEY_PATTERN =
            "/storages/{storageId}/{repositoryId}/{path:" + CustomAntPathMatcher.TWO_STARS_ANALOGUE + "}";

    @Inject
    @Named("customAntPathMatcher")
    private CustomAntPathMatcher antPathMatcher;

    @Override
    public void initialize(ValidAccessModelMapKey constraintAnnotation)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(Map<String, Collection<String>> map,
                           ConstraintValidatorContext context)
    {
        if (map == null || CollectionUtils.isEmpty(map))
        {
            return true;
        }

        final Map<String, String> uriTemplateVariables = new HashMap<>();
        for (Map.Entry<String, Collection<String>> e : map.entrySet())
        {
            String key = e.getKey();
            boolean matches = antPathMatcher.doMatch(KEY_PATTERN, key, true, uriTemplateVariables);
            if (!matches)
            {
                return false;
            }
        }

        return true;
    }

}
