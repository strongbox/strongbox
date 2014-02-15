package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.ConfigurationParser;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
@Scope ("singleton")
public class ConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private String configurationPath;

    private Configuration configuration;


    public ConfigurationManager()
    {
    }

    public void init()
            throws IOException
    {
        Resource resource = ConfigurationResourceResolver.getConfigurationResource("etc/configuration.xml",
                                                                                   "repository.config.xml",
                                                                                   "etc/configuration.xml");

        logger.info("Loading Strongbox configuration from " + resource.toString() + "...");

        ConfigurationParser parser = new ConfigurationParser();

        configuration = parser.parse(resource.getInputStream());
        configuration.setResource(resource);
        configuration.dump();
    }

    public void storeConfiguration(Configuration configuration, String file)
            throws IOException
    {
        ConfigurationParser parser = new ConfigurationParser();
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

}
