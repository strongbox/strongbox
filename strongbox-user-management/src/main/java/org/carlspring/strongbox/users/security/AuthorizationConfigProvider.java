package org.carlspring.strongbox.users.security;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class AuthorizationConfigProvider
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigProvider.class);

    private GenericParser<AuthorizationConfig> parser;

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;

    private AuthorizationConfig config;

    @PostConstruct
    public void init()
    {
        try
        {
            parser = new GenericParser<>(AuthorizationConfig.class);
            config = parser.parse(getConfigurationResource().getURL());

            logger.debug("Load authorization config from XLM file...");
            logger.debug(config.toString());
        }
        catch (Exception e)
        {
            logger.error("Unable to load authorization settings from XML file.", e);
        }
    }

    public Optional<AuthorizationConfig> getConfig()
    {
        return Optional.ofNullable(config);
    }

    private Resource getConfigurationResource()
            throws IOException
    {
        return configurationResourceResolver.getConfigurationResource("authorization.config.xml",
                                                                      "etc/conf/security-authorization.xml");
    }
}
