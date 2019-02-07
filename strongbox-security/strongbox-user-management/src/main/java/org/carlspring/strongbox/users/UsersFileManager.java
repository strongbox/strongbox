package org.carlspring.strongbox.users;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.xml.XmlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class UsersFileManager
        extends XmlFileManager<UsersDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


    @Override
    public String getPropertyKey()
    {
        return "strongbox.users.config.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-security-users.xml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
