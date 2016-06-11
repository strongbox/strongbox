package org.carlspring.strongbox.resource;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("configurationResourceResolver")
public class ConfigurationResourceResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResourceResolver.class);

    public ConfigurationResourceResolver()
    {
    }

    public Resource getConfigurationResource(String propertyKey,
                                             String propertyDefaultValue)
            throws IOException
    {
        final String configurationPath = ConfigurationResourceResolver.getVaultDirectory() + "/" + propertyDefaultValue;

        return getConfigurationResource(configurationPath, propertyKey, propertyDefaultValue);
    }

    /**
     *
     * @param configurationPath     The configuration file's path. If null, either propertyKey,
     *                              or propertyKeyDefaultValue must be specified.
     * @param propertyKey           The system property key to use when trying to load the configuration.
     * @param propertyDefaultValue  The default property key value.
     * @return
     * @throws IOException
     */
    public Resource getConfigurationResource(String configurationPath,
                                             String propertyKey,
                                             String propertyDefaultValue)
            throws IOException
    {
        String filename = null;
        Resource resource = null;

        if (configurationPath != null &&
            (!configurationPath.startsWith("classpath") && !(new File(configurationPath)).exists()))
        {
            configurationPath = null;
        }

        if (configurationPath != null)
        {
            if (configurationPath.toLowerCase().startsWith("classpath"))
            {
                // Load the resource from the classpath
                resource = new ClassPathResource(configurationPath);
            }
            else
            {
                // Load the resource from the file system
                resource = new FileSystemResource(new File(configurationPath).getAbsoluteFile());
            }
        }
        else
        {
            if (System.getProperty(propertyKey) != null)
            {
                filename = System.getProperty(propertyKey);
                resource = new FileSystemResource(new File(filename).getAbsoluteFile());
            }
            else
            {
                if (new File(propertyDefaultValue).exists())
                {
                    filename = propertyDefaultValue;
                    resource = new FileSystemResource(new File(filename).getAbsoluteFile());
                }
                else
                {
                    // This should only really be used for development and testing
                    // of Strongbox and is not advised for production.
                    resource = new ClassPathResource(propertyDefaultValue);
                }
            }
        }

        return resource;
    }

    public static String getHomeDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_HOME") != null ?
                               System.getenv("STRONGBOX_HOME") :
                               System.getProperty("strongbox.home");
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File(".").getAbsolutePath();
        }
    }

    public static String getVaultDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_VAULT") != null ?
                               System.getenv("STRONGBOX_VAULT") :
                               System.getProperty("strongbox.vault");
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File(".").getAbsolutePath();
        }
    }

}
