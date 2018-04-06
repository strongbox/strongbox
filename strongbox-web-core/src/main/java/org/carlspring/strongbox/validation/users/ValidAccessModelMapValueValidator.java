package org.carlspring.strongbox.validation.users;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Map;

import org.springframework.util.CollectionUtils;

/**
 * @author Pablo Tirado
 */
public class ValidAccessModelMapValueValidator
        implements ConstraintValidator<ValidAccessModelMapValue, Map<String, Collection<String>>>
{

    @Override
    public void initialize(ValidAccessModelMapValue constraintAnnotation)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(Map<String, Collection<String>> map,
                           ConstraintValidatorContext context)
    {
        if (map == null)
        {
            return true;
        }

        for (Collection<String> value : map.values())
        {
            if (CollectionUtils.isEmpty(value))
            {
                return false;
            }
        }

        return true;
    }

}
