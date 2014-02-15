package org.carlspring.strongbox.resource;

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
@Scope("singleton")
public class ConfigurationResourceResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResourceResolver.class);


    /**
     *
     * @param configurationPath     The configuration file's path. If null, either propertyKey,
     *                              or propertyKeyDefaultValue must be specified.
     * @param propertyKey           The system property key to use when trying to load the configuration.
     * @param propertyDefaultValue  The default property key value.
     * @return
     * @throws IOException
     */
    public static Resource getConfigurationResource(String configurationPath,
                                                    String propertyKey,
                                                    String propertyDefaultValue)
            throws IOException
    {
        String filename = null;
        Resource resource = null;

        if (configurationPath == null)
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

        logger.info("Loading configuration from " + resource.toString() + "...");

        return resource;
    }

}
