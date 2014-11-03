package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;

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
@Scope ("singleton")
public class ConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private String configurationPath;

    private Configuration configuration;

    private GenericParser<Configuration> parser = new GenericParser<Configuration>(Configuration.class);

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;


    public ConfigurationManager()
    {
    }

    @PostConstruct
    public void init()
            throws IOException, JAXBException
    {
        Resource resource = getConfigurationResource();

        logger.info("Loading Strongbox configuration from " + resource.toString() + "...");

        configuration = parser.parse(resource.getInputStream());
        configuration.setResource(resource);

        dump();
    }

    public void dump()
    {
        logger.info("Configuration version: " + configuration.getVersion());

        logger.info("Loading storages...");
        for (String storageKey : configuration.getStorages().keySet())
        {
            logger.info(" -> Storage: " + storageKey);

            Storage storage = configuration.getStorages().get(storageKey);
            for (String repositoryKey : storage.getRepositories().keySet())
            {
                logger.info("    -> Repository: " + repositoryKey);
            }
        }
    }

    public void store()
            throws IOException, JAXBException
    {
        store(configuration);
    }

    public void store(Configuration configuration)
            throws IOException, JAXBException
    {
        Resource resource = getConfigurationResource();

        parser.store(configuration, resource.getFile());
    }

    public void store(Configuration configuration, String file)
            throws IOException, JAXBException
    {
        parser.store(configuration, file);
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }

    public Resource getConfigurationResource()
            throws IOException
    {
        return configurationResourceResolver.getConfigurationResource("repository.config.xml",
                                                                      "etc/conf/strongbox.xml");
    }

}
