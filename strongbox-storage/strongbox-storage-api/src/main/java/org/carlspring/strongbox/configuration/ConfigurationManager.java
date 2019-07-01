package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.repository.RepositoryData;

import javax.inject.Inject;
import java.net.URI;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ConfigurationManager implements StoragesConfigurationManager
{

    @Inject
    private ConfigurationManagementService configurationService;

    public RepositoryData getRepository(String storageAndRepositoryId)
    {
        String[] elements = storageAndRepositoryId.split(":");
        String storageId = elements[0];
        String repositoryId = elements[1];

        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public RepositoryData getRepository(String storageId,
                                    String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public StorageData getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }
    
    public Configuration getConfiguration()
    {
        return configurationService.getConfiguration();
    }

    public URI getBaseUri()
    {
        try
        {
            return URI.create(getConfiguration().getBaseUrl());
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidConfigurationException(e);
        }
    }

    public Integer getSessionTimeoutSeconds() {
        return getConfiguration().getSessionConfiguration().getTimeoutSeconds();
    }
}
