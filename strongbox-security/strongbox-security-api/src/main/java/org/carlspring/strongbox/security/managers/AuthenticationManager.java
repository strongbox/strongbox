package org.carlspring.strongbox.security.managers;

import org.carlspring.strongbox.configuration.AnonymousAccessConfiguration;
import org.carlspring.strongbox.configuration.AuthenticationConfiguration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
@Scope("singleton")
public class AuthenticationManager
        implements AuthenticationConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    private AuthenticationConfiguration configuration;

    private GenericParser<AuthenticationConfiguration> parser = new GenericParser<>(AuthenticationConfiguration.class);


    @Override
    public void load()
            throws IOException, JAXBException
    {
        Resource resource = ConfigurationResourceResolver.getConfigurationResource("security.authentication.xml",
                                                                                   "etc/conf/security-authentication.xml");

        logger.info("Loading Strongbox configuration from " + resource.toString() + "...");

        configuration = parser.parse(resource.getInputStream());
    }

    @Override
    public void store()
            throws IOException, JAXBException
    {
        Resource resource = ConfigurationResourceResolver.getConfigurationResource("security.authentication.xml",
                                                                                   "etc/conf/security-authentication.xml");

        parser.store(configuration, resource);
    }

    public List<String> getRealms()
    {
        return configuration.getRealms();
    }

    public boolean removeRealm(String realm)
    {
        return configuration.removeRealm(realm);
    }

    public boolean addRealm(String realm)
    {
        return configuration.addRealm(realm);
    }

    public void setRealms(List<String> realms)
    {
        configuration.setRealms(realms);
    }

    public boolean containsRealm(String realm)
    {
        return configuration.containsRealm(realm);
    }

    public void setAnonymousAccessConfiguration(AnonymousAccessConfiguration anonymousAccessConfiguration)
    {
        configuration.setAnonymousAccessConfiguration(anonymousAccessConfiguration);
    }

    public AnonymousAccessConfiguration getAnonymousAccessConfiguration()
    {
        return configuration.getAnonymousAccessConfiguration();
    }

    public AuthenticationConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(AuthenticationConfiguration configuration)
    {
        this.configuration = configuration;
    }

}
