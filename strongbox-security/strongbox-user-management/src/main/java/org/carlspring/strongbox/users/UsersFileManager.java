package org.carlspring.strongbox.users;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@Component
public class UsersFileManager
        extends YamlFileManager<UsersDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

    @Inject
    public UsersFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory);
    }


    @Override
    public String getPropertyKey()
    {
        return "strongbox.users.config.yaml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-security-users.yaml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
