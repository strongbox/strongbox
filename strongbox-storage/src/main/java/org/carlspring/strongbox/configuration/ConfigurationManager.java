package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.url.ClasspathURLStreamHandler;
import org.carlspring.strongbox.xml.parsers.ConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
        String filename =  null;
        Resource resource = null;

        if (configurationPath == null)
        {
            if (System.getProperty("repository.config.xml") != null)
            {
                filename =  System.getProperty("repository.config.xml");
                resource = new FileSystemResource(new File(filename).getAbsoluteFile());
            }
            else
            {
                if (new File("etc/configuration.xml").exists())
                {
                    filename =  "etc/configuration.xml";
                    resource = new FileSystemResource(new File(filename).getAbsoluteFile());
                }
                else
                {
                    // This should only really be used for development and testing
                    // of Strongbox and is not advised for production.
                    String path = "etc/configuration.xml";
                    resource = new ClassPathResource(path);
                }
            }
        }
        else
        {
            if (!configurationPath.toLowerCase().startsWith("classpath"))
            {
                resource = new FileSystemResource(new File(configurationPath).getAbsoluteFile());
            }
            else
            {
                resource = new ClassPathResource(configurationPath);
            }
        }

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
