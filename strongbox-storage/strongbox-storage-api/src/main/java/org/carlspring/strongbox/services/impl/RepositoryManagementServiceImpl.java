package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryManagementService")
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        if (provider != null)
        {
            provider.getRepositoryManagementStrategy().createRepository(storageId, repositoryId);
        }
        else
        {
            Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

            logger.warn("Layout provider '" + repository.getLayout() + "' could not be resolved. " +
                        "Using generic implementation instead.");

            File repositoryDir = new File(repository.getStorage().getBasedir(), repositoryId);
            if (!repositoryDir.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                repositoryDir.mkdirs();
            }
        }
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        provider.getRepositoryManagementStrategy().removeRepository(storageId, repositoryId);
    }

    private LayoutProvider getLayoutProvider(String storageId,
                                             String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
