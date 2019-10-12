package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@Component
public class UsersFileManager
        extends YamlFileManager<UsersDto>
{
    @Value("#{@propertiesPathResolver.resolve('strongbox.users.config.yaml','etc/conf/strongbox-security-users.yaml')}")
    private Resource resource;

    @Inject
    public UsersFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory);
    }

    @Override
    protected Resource getResource()
    {
        return resource;
    }
}
