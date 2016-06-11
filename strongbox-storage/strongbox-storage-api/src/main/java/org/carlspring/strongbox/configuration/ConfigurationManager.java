package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

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
public class ConfigurationManager extends AbstractConfigurationManager<Configuration>
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;


    public ConfigurationManager()
    {
        super(Configuration.class);
        logger.info("Initilizing ConfigurationManager");
    }

    @PostConstruct
    public void init()
            throws IOException, JAXBException
    {
        super.init();

        setRepositoryStorageRelationships();
        setAllows();

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
     * Sets the repository <--> storage relationships explicitly, as initially,
     * when these are deserialized from the XML, they have no such relationship.
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

    public void dump()
    {
        logger.info("Configuration version: " + getConfiguration().getVersion());

        if (!getConfiguration().getStorages().isEmpty())
        {
            logger.info("Loading storages...");
            for (String storageKey : getConfiguration().getStorages().keySet())
            {
                logger.info(" -> Storage: " + storageKey);

                Storage storage = getConfiguration().getStorages().get(storageKey);
                for (String repositoryKey : storage.getRepositories().keySet())
                {
                    logger.info("    -> Repository: " + repositoryKey);
                }
            }
        }
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

    @Override
    public Resource getConfigurationResource() throws IOException {
        return configurationResourceResolver.getConfigurationResource("repository.config.xml", "etc/conf/strongbox.xml");
    }

}
