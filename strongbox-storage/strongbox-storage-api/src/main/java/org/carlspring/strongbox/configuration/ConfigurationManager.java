package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
@Scope("singleton")
public class ConfigurationManager
        extends AbstractConfigurationManager<Configuration>
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;

    @Autowired
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    public ConfigurationManager()
    {
        super(Configuration.class);
        logger.debug("Initializing configuration...");
    }

    @PostConstruct
    public synchronized void init()
            throws IOException
    {
        try
        {
            super.init();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        setRepositoryStorageRelationships();
        setAllows();
        setProxyRepositoryConnectionPoolConfigurations();

        dump();
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
                        if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()) ||
                            repository.getType().equals(RepositoryTypeEnum.PROXY.getType()))
                        {
                            repository.setAllowsDelete(false);
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
                     .forEach(repository ->
                              {
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

        final File lockFile = new File(ConfigurationResourceResolver.getVaultDirectory(), "storage-booter.lock");
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
            logger.warn("Storages and repositories appear to have already been loaded. (" + lockFile.getAbsolutePath() +
                        " already exists).");
        }
    }

    public Repository getRepository(String storageAndRepositoryId)
    {
        String[] elements = storageAndRepositoryId.split(":");
        String storageId = elements[0];
        String repositoryId = elements[1];

        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public String getStorageId(Storage storage,
                               String storageAndRepositoryId)
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

    @Override
    public Resource getConfigurationResource()
            throws IOException
    {
        return configurationResourceResolver.getConfigurationResource("repository.config.xml",
                                                                      "etc/conf/strongbox.xml");
    }

}
