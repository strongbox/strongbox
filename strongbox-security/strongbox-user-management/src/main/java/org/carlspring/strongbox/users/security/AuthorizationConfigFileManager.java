package org.carlspring.strongbox.users.security;

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
public class AuthorizationConfigFileManager
{

    private final GenericParser<AuthorizationConfig> parser = new GenericParser<>(AuthorizationConfig.class);

    private Resource getConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("authorization.config.xml",
                                                                      "etc/conf/strongbox-authorization.xml");
    }

    public void store(final AuthorizationConfig config)
    {
        try
        {
            parser.store(config, getConfigurationResource().getFile());
        }
        catch (JAXBException | IOException e)
        {
            throw new AuthorizationConfigSaveException(e);
        }
    }

    public AuthorizationConfig read()
    {
        try
        {
            return parser.parse(getConfigurationResource().getFile());
        }
        catch (JAXBException | IOException e)
        {
            throw new AuthorizationConfigReadException(e);
        }
    }

}
