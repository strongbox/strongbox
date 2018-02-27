package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ConfigurationFileManager
{

    private final GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

    public Resource getConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.config.xml", "etc/conf/strongbox.xml");
    }

    public void store(final Configuration configuration)
            throws IOException, JAXBException
    {
        parser.store(configuration, getConfigurationResource().getFile());
    }

    public Configuration read()
            throws IOException, JAXBException
    {
        return parser.parse(getConfigurationResource().getFile());
    }


}
