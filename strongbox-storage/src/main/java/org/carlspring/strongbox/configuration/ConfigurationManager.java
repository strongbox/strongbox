package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.url.ClasspathURLStreamHandler;
import org.carlspring.strongbox.xml.parsers.ConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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

    private String configurationFile;

    private Configuration configuration;


    public ConfigurationManager()
    {
    }

    public void init()
            throws IOException
    {
        URL url = null;
        String filename =  null;

        if (configurationFile == null)
        {
            if (System.getProperty("repository.config.xml") != null)
            {
                filename =  System.getProperty("repository.config.xml");
                url = new File(filename).getAbsoluteFile().toURI().toURL();
            }
            else
            {
                if (new File("etc/configuration.xml").exists())
                {
                    filename =  "etc/configuration.xml";
                    url = new File(filename).getAbsoluteFile().toURI().toURL();
                }
                else
                {
                    // This should only really be used for development and testing
                    // of Strongbox and is not advised for production.
                    String path = "classpath:etc/configuration.xml";
                    url = new URL(null, path, new ClasspathURLStreamHandler(ClassLoader.getSystemClassLoader()));
                }
            }
        }
        else
        {
            url = new File(configurationFile).getAbsoluteFile().toURI().toURL();
        }

        ConfigurationParser parser = new ConfigurationParser();

        configuration = parser.parse(url);
        if (configurationFile != null)
        {
            configuration.setFilename(filename);
        }
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
