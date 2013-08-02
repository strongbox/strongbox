package org.carlspring.repositoryunit.configuration;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private static boolean initialized = false;

    private static ConfigurationManager instance;

    private Configuration configuration;


    public static ConfigurationManager getInstance()
            throws IOException
    {
        if (instance == null)
        {
            instance = new ConfigurationManager();
            initialize();
        }

        return instance;
    }

    public static void initialize()
            throws IOException
    {
        if (!initialized)
        {
            getInstance().loadConfiguration();

            initialized = true;
        }
    }

    public void loadConfiguration()
            throws IOException
    {
        String filename = System.getProperty("repository.config.xml") != null ?
                          System.getProperty("repository.config.xml") :
                          "etc/configuration.xml";

        File file = new File(filename);
        if (!file.exists() && System.getProperty("repository.config.xml") == null)
        {
            logger.warn("A configuration will not be loaded, as no etc/configuration.xml file was found," +
                        " nor a value for 'repository.config.xml' was defined.");

            return;
        }

        ConfigurationParser parser = new ConfigurationParser();

        configuration = parser.parseConfiguration(filename);
        configuration.setFilename(filename);
        configuration.dump();
    }

    public void storeConfiguration(Configuration configuration, String file)
            throws IOException
    {
        ConfigurationParser parser = new ConfigurationParser();
        parser.storeConfiguration(configuration, file);
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

}
