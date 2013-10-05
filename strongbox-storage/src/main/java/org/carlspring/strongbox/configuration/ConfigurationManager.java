package org.carlspring.strongbox.configuration;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 * @author mtodorov
 */
@Scope ("singleton")
public class ConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private static boolean initialized = false;

    private String configurationFile;

    private Configuration configuration;


    public ConfigurationManager()
    {
    }

    public void init()
            throws IOException
    {
        String filename;

        if (configurationFile == null)
        {
            filename = System.getProperty("repository.config.xml") != null ?
                       System.getProperty("repository.config.xml") :
                       "etc/configuration.xml";
        }
        else
        {
            filename = configurationFile;
        }

        File file = new File(filename).getCanonicalFile();

        logger.debug("Using configuration file " + file.getCanonicalPath() + "...");

        if (!file.exists() && System.getProperty("repository.config.xml") == null)
        {
            logger.warn("A configuration will not be loaded, as no etc/configuration.xml file was found," +
                        " nor a value for 'repository.config.xml' was defined.");

            return;
        }
        else
        {
            logger.debug("Loaded configuration from " + file.getCanonicalPath() + "...");
        }

        ConfigurationParser parser = new ConfigurationParser();

        configuration = parser.parse(filename);
        configuration.setFilename(filename);
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

    public String getConfigurationFile()
    {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile)
    {
        this.configurationFile = configurationFile;
    }

}
