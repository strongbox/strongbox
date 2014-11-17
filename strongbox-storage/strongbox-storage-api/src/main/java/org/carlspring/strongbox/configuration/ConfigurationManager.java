package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;

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
public class ConfigurationManager extends AbstractConfigurationManager<Configuration>
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private String configurationPath;

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;


    public ConfigurationManager()
    {
        super(Configuration.class);
    }

    @PostConstruct
    public void init()
            throws IOException, JAXBException
    {
        super.init();
        dump();
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

    @SuppressWarnings("unchecked")
    @Override
    public Configuration getConfiguration()
    {
        return (Configuration) super.getConfiguration();
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
