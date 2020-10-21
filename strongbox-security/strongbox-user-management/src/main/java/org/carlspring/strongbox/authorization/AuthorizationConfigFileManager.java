package org.carlspring.strongbox.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
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
public class AuthorizationConfigFileManager
        extends YamlFileManager<AuthorizationConfigDto>
{
    @Value("#{@propertiesPathResolver.resolve('strongbox.authorization.config.yaml','etc/conf/strongbox-authorization.yaml')}")
    private Resource resource;

    @Inject
    public AuthorizationConfigFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory);
    }

    @Override
    protected Resource getResource()
    {
        return resource;
    }
}
