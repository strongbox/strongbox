package org.carlspring.strongbox.validation.cron.autocomplete;

import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;

import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class CronTaskConfigurationFormFieldRepositoryIdAutocompleteValidator
        implements CronTaskConfigurationFormFieldAutocompleteValidator
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Override
    public boolean isValid(String value)
    {
        Configuration configuration = configurationManagementService.getConfiguration();
        Map<String, Storage> storages = configuration.getStorages();
        return storages.keySet()
                       .stream()
                       .filter(sId -> configuration.getStorage(sId).getRepositories().keySet().contains(value))
                       .findFirst()
                       .isPresent();
    }

    @Override
    public boolean supports(String name)
    {
        return REPOSITORY_ID.equals(name);
    }
}
