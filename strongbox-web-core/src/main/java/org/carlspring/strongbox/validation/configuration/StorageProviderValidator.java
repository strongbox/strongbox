package org.carlspring.strongbox.validation.configuration;

import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author carlspring
 */
public class StorageProviderValidator
        implements ConstraintValidator<StorageProviderValue, String>
{

    @Inject
    private StorageProviderRegistry storageProviderRegistry;


    @Override
    public void initialize(StorageProviderValue constraint)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(String alias,
                           ConstraintValidatorContext context)
    {
        final StorageProvider storageProvider = storageProviderRegistry.getProvider(alias);

        return storageProvider != null;
    }

}
