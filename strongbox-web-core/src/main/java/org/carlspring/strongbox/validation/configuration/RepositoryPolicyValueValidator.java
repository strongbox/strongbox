package org.carlspring.strongbox.validation.configuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryPolicyValueValidator
        implements ConstraintValidator<RepositoryPolicyValue, String>
{

    @Override
    public void initialize(final RepositoryPolicyValue constraintAnnotation)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext context)
    {
        throw new UnsupportedOperationException();
    }
}
