package org.carlspring.strongbox.validation.configuration;

import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Przemyslaw Fusik
 */
public class LayoutProviderValueValidator
        implements ConstraintValidator<LayoutProviderValue, String>
{

    private boolean allowNull;

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public void initialize(final LayoutProviderValue constraintAnnotation)
    {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext context)
    {
        if (value == null)
        {
            return allowNull;
        }

        return layoutProviderRegistry.getProviders().keySet().contains(value);
    }
}
