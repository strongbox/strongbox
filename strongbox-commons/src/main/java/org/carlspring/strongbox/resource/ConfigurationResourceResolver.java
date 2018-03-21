package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.data.PropertyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
public class ConfigurationResourceResolver
{


    public static Resource getConfigurationResource(String propertyKey,
                                                    String propertyDefaultValue)
            throws IOException
    {
        final String configurationPath = ConfigurationResourceResolver.getHomeDirectory() + "/" + propertyDefaultValue;

        return getConfigurationResource(configurationPath, propertyKey, propertyDefaultValue);
    }

    /**
     * @param configurationPath    The configuration file's path. If null, either propertyKey,
     *                             or propertyKeyDefaultValue must be specified.
     * @param propertyKey          The system property key to use when trying to load the configuration.
     * @param propertyDefaultValue The default property key value.
     * @return
     * @throws IOException
     */
    public static Resource getConfigurationResource(String configurationPath,
                                                    String propertyKey,
                                                    String propertyDefaultValue)
            throws IOException
    {
        String filename;
        Resource resource;

        if (configurationPath != null &&
            (!configurationPath.startsWith("classpath") && !(new File(configurationPath)).exists()))
        {
            configurationPath = null;
        }

        if (configurationPath != null)
        {
            if (configurationPath.toLowerCase()
                                 .startsWith("classpath"))
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
        return PropertyUtils.getHomeDirectory();
    }

    public static String getVaultDirectory()
    {
        return PropertyUtils.getVaultDirectory();
    }

    public static String getTempDirectory()
            throws IOException
    {
        final String tempDirectory = PropertyUtils.getTempDirectory();
        final Path tempDirectoryPath = Paths.get(tempDirectory);
        if (Files.notExists(tempDirectoryPath))
        {
            Files.createDirectories(tempDirectoryPath);
        }
        return tempDirectory;
    }

    public static String getEtcDirectory()
    {
        return PropertyUtils.getEtcDirectory();
    }
}
