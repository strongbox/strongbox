package org.carlspring.strongbox.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.XmlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class AuthorizationConfigFileManager
        extends XmlFileManager<AuthorizationConfigDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


    @Override
    public String getPropertyKey()
    {
        return "strongbox.authorization.config.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-authorization.xml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
