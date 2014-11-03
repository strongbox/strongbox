package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ConfigurationManagementServiceImpl implements ConfigurationManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImpl.class);

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;


    @Override
    public String getBaseUrl()
            throws IOException
    {
        return configurationManager.getConfiguration().getBaseUrl();
    }

    @Override
    public void setBaseUrl(String baseUrl)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().setBaseUrl(baseUrl);
        configurationManager.store();
    }

    @Override
    public int getPort()
            throws IOException
    {
        return configurationManager.getConfiguration().getPort();
    }

    @Override
    public void setPort(int port)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().setPort(port);
        configurationManager.store();
    }

    @Override
    public void addOrUpdateStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().addStorage(storage);
        configurationManager.store();

        if (!storage.existsOnFileSystem())
        {
            final File storageBaseDir = new File(storage.getBasedir(), storage.getId());

            logger.debug("Creating directory for storage '" + storage.getId() +
                         "' (" + storageBaseDir.getAbsolutePath() + ")...");

            //noinspection ResultOfMethodCallIgnored
            storageBaseDir.mkdirs();
        }
    }

    @Override
    public Storage getStorage(String storageId)
            throws IOException
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    @Override
    public void removeStorage(String storageId)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().getStorages().remove(storageId);
        configurationManager.store();
    }

    @Override
    public void addOrUpdateRepository(String storageId, Repository repository)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().getStorage(storageId).addOrUpdateRepository(repository);
        configurationManager.store();
    }

    @Override
    public Repository getRepository(String storageId,
                                    String repositoryId)
            throws IOException
    {
        return configurationManager.getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    @Override
    public void removeRepository(String storageId, String repositoryId)
            throws IOException, JAXBException
    {
        configurationManager.getConfiguration().getStorage(storageId).removeRepository(repositoryId);
        configurationManager.store();
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

    public void setConfigurationResourceResolver(ConfigurationResourceResolver configurationResourceResolver)
    {
        this.configurationResourceResolver = configurationResourceResolver;
    }

}
