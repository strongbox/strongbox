package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.support.ConfigurationReadException;
import org.carlspring.strongbox.services.support.ConfigurationSaveException;
import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ConfigurationFileManager
{
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileManager.class);

    private final GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

    public Resource getConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.config.xml", "etc/conf/strongbox.xml");
    }

    public void store(final Configuration configuration)
    {
        try
        {
            parser.store(configuration, getConfigurationResource());
        }
        catch (JAXBException | IOException e)
        {
            throw new ConfigurationSaveException(e);
        }
    }

    public Configuration read()
    {
        try(InputStream inputStream = getConfigurationResource().getInputStream())
        {
            return parser.parse(inputStream);
        }
        catch (JAXBException | IOException e)
        {
            throw new ConfigurationReadException(e);
        }
    }

}
