package org.carlspring.strongbox.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
public class ConfigurationResourceResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResourceResolver.class);

    public static Resource getConfigurationResource(String propertyKey,
                                                    String propertyDefaultValue)
    {
        final String configurationPath = propertyDefaultValue != null && !propertyDefaultValue.startsWith("classpath:")
                ? ConfigurationResourceResolver.getHomeDirectory() + "/" + propertyDefaultValue
                : propertyDefaultValue;

        return getConfigurationResource(configurationPath, propertyKey, propertyDefaultValue);
    }

    /**
     * @param configurationPath
     *            The configuration file's path. If null, either propertyKey,
     *            or propertyKeyDefaultValue must be specified.
     * @param propertyKey
     *            The system property key to use when trying to load the
     *            configuration.
     * @param propertyDefaultValue
     *            The default property key value.
     * @return
     * @throws IOException
     */
    public static Resource getConfigurationResource(String configurationPath,
                                                    String propertyKey,
                                                    String propertyDefaultValue)
    {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        resourceLoader.addProtocolResolver((l,
                                            r) -> {

            File file = new File(l);
            if (!file.exists())
            {
                return null;
            }

            return new FileSystemResource(file);
        });

        if (System.getProperty(propertyKey) != null)
        {
            String filename = System.getProperty(propertyKey);

            logger.info(String.format("Using configured resource path [%s]", filename));

            return resourceLoader.getResource(filename);
        }

        logger.info(String.format("Try to fetch configuration resource path [%s]", configurationPath));

        if (configurationPath != null &&
                (!configurationPath.startsWith("classpath") && !(Files.exists(Paths.get(configurationPath)))))
        {
            logger.info(String.format(
                                      "Configuration resource [%s] does not exist, will try to resolve with configured location [%s].",
                                      configurationPath, propertyKey));

            configurationPath = null;
        }

        if (configurationPath != null)
        {
            logger.info(String.format("Using provided resource path [%s]", configurationPath));

            return resourceLoader.getResource(configurationPath);
        }
        else
        {
            logger.info(String.format("Using default resource path [%s]", propertyDefaultValue));

            return resourceLoader.getResource(propertyDefaultValue);
        }
    }

    public static String getHomeDirectory()
    {
        return PropertiesBooter.getHomeDirectory();
    }

    public static String getVaultDirectory()
    {
        return PropertiesBooter.getVaultDirectory();
    }

    public static String getTempDirectory()
        throws IOException
    {
        final String tempDirectory = PropertiesBooter.getTempDirectory();
        final Path tempDirectoryPath = Paths.get(tempDirectory);
        if (Files.notExists(tempDirectoryPath))
        {
            Files.createDirectories(tempDirectoryPath);
        }

        return tempDirectory;
    }

    public static String getEtcDirectory()
    {
        return PropertiesBooter.getEtcDirectory();
    }
}
