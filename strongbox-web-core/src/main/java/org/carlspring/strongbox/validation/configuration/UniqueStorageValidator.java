package org.carlspring.strongbox.validation.configuration;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Pablo Tirado
 */
public class UniqueStorageValidator
        implements ConstraintValidator<UniqueStorage, String>
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Override
    public void initialize(UniqueStorage constraint)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(String storageId,
                           ConstraintValidatorContext context)
    {
        final Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
        return storage == null;
    }

}
