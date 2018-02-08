package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ConfigurationManager
        extends AbstractConfigurationManager<Configuration>
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;


    @PostConstruct
    public synchronized void init()
            throws IOException, JAXBException
    {
        super.init();

        logger.debug("Initializing configuration...");

        setRepositoryStorageRelationships();
        setRepositoryArtifactCoordinateValidators();
        setAllows();
        setProxyRepositoryConnectionPoolConfigurations();

        dump();
    }

    private void setRepositoryArtifactCoordinateValidators()
    {
        // TODO: Implement this

        final Configuration configuration = getConfiguration();
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

                        Set<String> defaultArtifactCoordinateValidators = layoutProvider.getDefaultArtifactCoordinateValidators();
                        if (repository.getArtifactCoordinateValidators() == null &&
                            defaultArtifactCoordinateValidators != null)
                        {
                            repository.setArtifactCoordinateValidators(defaultArtifactCoordinateValidators);
                        }
                    }
                }
            }
        }

        configurationRepository.updateConfiguration(configuration);
    }

    private void setAllows()
    {
        final Configuration configuration = getConfiguration();
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
                        {
                            repository.setAllowsDelete(false);
                            repository.setAllowsDeployment(false);
                            repository.setAllowsRedeployment(false);
                        }
                        if (repository.getType().equals(RepositoryTypeEnum.PROXY.getType()))
                        {
                            repository.setAllowsDeployment(false);
                            repository.setAllowsRedeployment(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the repository <--> storage relationships explicitly, as initially, when these are deserialized from the
     * XML, they have no such relationship.
     */
    public void setRepositoryStorageRelationships()
    {
        final Configuration configuration = getConfiguration();
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        repository.setStorage(storage);
                    }
                }
            }
        }

        configurationRepository.updateConfiguration(configuration);
    }

    private void setProxyRepositoryConnectionPoolConfigurations()
    {
        final Configuration configuration = getConfiguration();
        configuration.getStorages().values().stream()
                     .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                     .flatMap(storage -> storage.getRepositories().values().stream())
                     .forEach(repository -> {
                         if (repository.getHttpConnectionPool() != null
                             && repository.getRemoteRepository() != null &&
                             repository.getRemoteRepository().getUrl() != null)
                         {
                             proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                     repository.getRemoteRepository().getUrl(),
                                     repository.getHttpConnectionPool().getAllocatedConnections());
                         }
                     });
    }

    public void dump()
    {
        logger.info("Configuration version: " + getConfiguration().getVersion());

        final File lockFile = getStrongboxLockFile();
        if (!lockFile.exists())
        {
            if (!getConfiguration().getStorages().isEmpty())
            {
                logger.info("Loading storages...");
                for (String storageKey : getConfiguration().getStorages().keySet())
                {
                    logger.info(" -> Storage: " + storageKey);
                    if (storageKey == null)
                    {
                        throw new IllegalArgumentException("Null keys do not supported");
                    }

                    Storage storage = getConfiguration().getStorages().get(storageKey);
                    for (String repositoryKey : storage.getRepositories().keySet())
                    {
                        logger.info("    -> Repository: " + repositoryKey);
                    }
                }
            }
        }
        else
        {
            logger.warn("Storages and repositories appear to have already been loaded. (" + lockFile.getAbsolutePath() + " already exists).");
        }
    }

    public static File getStrongboxLockFile()
    {
        return new File(ConfigurationResourceResolver.getVaultDirectory(), "storage-booter.lock");
    }

    public Repository getRepository(String storageAndRepositoryId)
    {
        String[] elements = storageAndRepositoryId.split(":");
        String storageId = elements[0];
        String repositoryId = elements[1];

        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public Repository getRepository(String storageId, String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public String getStorageId(Storage storage, String storageAndRepositoryId)
    {
        String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");

        return storageAndRepositoryIdTokens.length == 2 ? storageAndRepositoryIdTokens[0] : storage.getId();
    }

    public String getRepositoryId(String storageAndRepositoryId)
    {
        String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");

        return storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Configuration getConfiguration()
    {
        return (Configuration) this.configuration;
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
    
    @Override
    public synchronized Resource getConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.config.xml",
                                                                      "etc/conf/strongbox.xml");
    }

}
