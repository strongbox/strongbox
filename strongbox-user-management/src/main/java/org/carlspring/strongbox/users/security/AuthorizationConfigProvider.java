package org.carlspring.strongbox.users.security;

import org.carlspring.strongbox.configuration.ConfigurationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.jaas.Privilege;
import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Responsible for load, validate and save to the persistent storage {@link AuthorizationConfig} from configuration sources.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-126}
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

        getConfig().ifPresent(this::validateConfig);
    }

    private void validateConfig(AuthorizationConfig config)
            throws ConfigurationException
    {

        // check that embedded privileges was not overridden
        Sets.SetView<String> privilegesIntersection = toIntersection(config.getPrivileges().getPrivileges(),
                                                                     Privileges.all(),
                                                                     o -> ((Privilege) o).getName().toUpperCase(),
                                                                     o -> ((Privileges) o).name().toUpperCase());
        if (!privilegesIntersection.isEmpty())
        {
            throw new ConfigurationException("Embedded privileges overriding is forbidden: " + privilegesIntersection);
        }

        // check that embedded roles was not overridden
        Sets.SetView<String> rolesIntersection = toIntersection(config.getRoles().getRoles(),
                                                                Arrays.asList(Roles.values()),
                                                                o -> ((Role) o).getName().toUpperCase(),
                                                                o -> ((Roles) o).name().toUpperCase());
        if (!rolesIntersection.isEmpty())
        {
            throw new ConfigurationException("Embedded roles overriding is forbidden: " + rolesIntersection);
        }
    }

    /**
     * Calculates intersection of two sets that was created from two iterable sources with help of
     * two name functions respectively.
     */
    private Sets.SetView<String> toIntersection(Iterable<?> first,
                                                Iterable<?> second,
                                                NameFunction firstNameFunction,
                                                NameFunction secondNameFunction)
    {
        Set<String> firstNames = new HashSet<>();
        Set<String> secondNames = new HashSet<>();

        for (Object o : first)
        {
            firstNames.add(firstNameFunction.name(o));
        }

        for (Object o : second)
        {
            secondNames.add(secondNameFunction.name(o));
        }

        return Sets.intersection(firstNames, secondNames);
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

    private interface NameFunction
    {

        String name(Object o);
    }
}
