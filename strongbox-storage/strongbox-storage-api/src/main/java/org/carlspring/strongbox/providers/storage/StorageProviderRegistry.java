package org.carlspring.strongbox.providers.storage;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.AbstractMappedProviderRegistryWithNestedMap;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author carlspring
 */
@Component("storageProviderRegistry")
public class StorageProviderRegistry extends AbstractMappedProviderRegistryWithNestedMap<StorageProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(StorageProviderRegistry.class);

    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private ConfigurationManager configurationManager;


    public StorageProviderRegistry()
    {
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the storage provider registry.");
    }

    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                if (repository.allowsDeletion())
                {
                    logger.debug("Emptying trash for repository " + repository.getId() + "...");

                    final File basedirTrash = repository.getTrashDir();

                    FileUtils.deleteDirectory(basedirTrash);

                    //noinspection ResultOfMethodCallIgnored
                    basedirTrash.mkdirs();
                }
                else
                {
                    logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
                }
            }
        }
    }

    public void undeleteTrash()
            throws IOException,
                   ProviderImplementationException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                StorageProvider storageProvider = getStorageProvider(repository, this);

                storageProvider.undeleteTrash(storage.getId(), repository.getId());
            }
        }
    }

    @Override
    public Map<String, Map<String, StorageProvider>> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, Map<String, StorageProvider>> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public Map<String, StorageProvider> getProviderImplementations(String alias)
    {
        return super.getProviderImplementations(alias);
    }

    @Override
    public StorageProvider getProviderImplementation(String alias, String implementation)
            throws ProviderImplementationException
    {
        return super.getProviderImplementation(alias, implementation);
    }

    @Override
    public void addProviderImplementation(String alias, String implementation, StorageProvider provider)
    {
        super.addProviderImplementation(alias, implementation, provider);
    }

    @Override
    public void removeProviderImplementation(String alias, String implementation)
    {
        super.removeProviderImplementation(alias, implementation);
    }

    public static StorageProvider getStorageProvider(Repository repository,
                                                     StorageProviderRegistry storageProviderRegistry)
            throws ProviderImplementationException
    {
        return storageProviderRegistry.getProviderImplementation(repository.getImplementation(),
                                                                 repository.getLayout());
    }

    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
