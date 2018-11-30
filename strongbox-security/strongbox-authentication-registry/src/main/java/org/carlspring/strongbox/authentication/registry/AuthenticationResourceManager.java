package org.carlspring.strongbox.authentication.registry;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class AuthenticationResourceManager
{

    private static final String PROPERTY_AUTHENTICATION_PROVIDERS_LOCATION = "strongbox.authentication.providers.xml";

    private static final String DEFAULT_AUTHENTICATION_PROVIDERS_LOCATION = "classpath:strongbox-authentication-providers.xml";

    private static final String PROPERTY_AUTHENTICATION_PROVIDERS_CONFIG_LOCATION = "strongbox.authentication.providers.yaml";

    private static final String DEFAULT_AUTHENTICATION_PROVIDERS_CONFIG_LOCATION = "classpath:strongbox-authentication-providers.yaml";

    private Resource authenticationConfigurationResource;

    private Resource authenticationPropertiesResource;

    @PostConstruct
    private void init()
        throws IOException
    {
        authenticationConfigurationResource = doGetResource(ConfigurationResourceResolver.getConfigurationResource(PROPERTY_AUTHENTICATION_PROVIDERS_LOCATION,
                                                                                                                   DEFAULT_AUTHENTICATION_PROVIDERS_LOCATION));

        authenticationPropertiesResource = doGetResource(ConfigurationResourceResolver.getConfigurationResource(PROPERTY_AUTHENTICATION_PROVIDERS_CONFIG_LOCATION,
                                                                                                                DEFAULT_AUTHENTICATION_PROVIDERS_CONFIG_LOCATION));
    }

    public Resource getAuthenticationConfigurationResource()
        throws IOException
    {
        return authenticationConfigurationResource;
    }

    public Resource getAuthenticationPropertiesResource()
        throws IOException
    {
        return authenticationPropertiesResource;
    }

    private Resource doGetResource(Resource resource)
        throws IOException
    {
        if (!resource.isFile() || resource instanceof ClassPathResource)
        {
            return new InMemoryResource(StreamUtils.copyToByteArray(resource.getInputStream()));
        }

        return resource;
    }

}
