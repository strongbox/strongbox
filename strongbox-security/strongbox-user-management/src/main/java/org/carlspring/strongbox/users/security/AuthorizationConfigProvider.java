package org.carlspring.strongbox.users.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.configuration.ConfigurationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.users.service.AuthorizationConfigService;
import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.exception.OSerializationException;

/**
 * Responsible for load, validate and save to the persistent storage {@link AuthorizationConfig} from configuration
 * sources.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain=https://dev.carlspring.org/youtrack/issue/SB-126}
 */
@Component
public class AuthorizationConfigProvider
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigProvider.class);

    @Inject
    private AuthorizationConfigService configService;
    private GenericParser<AuthorizationConfig> parser;
    @Inject
    private OEntityManager oEntityManager;
    private AuthorizationConfig config;
    @Inject
    private TransactionTemplate transactionTemplate;

    private static void throwIfNotEmpty(Sets.SetView<String> intersectionView,
                                        String message)
    {
        if (!intersectionView.isEmpty())
        {
            throw new ConfigurationException(message + intersectionView);
        }
    }

    private static Set<String> collect(@NotNull Iterable<?> it,
                                       @NotNull NameFunction nameFunction)
    {
        Set<String> names = new HashSet<>();
        for (Object o : it)
        {
            names.add(nameFunction.name(o));
        }
        return names;
    }

    @PostConstruct
    public void init()
        throws IOException,
        JAXBException
    {
        // check database for any configuration source, if something is already in place
        // reuse it and skip reading configuration from XML
        transactionTemplate.execute((s) -> {
            try
            {
                doInit();
            }
            catch (Exception e)
            {
                throw new BeanInitializationException(String.format("Failed to initialize: msg-[%s]", e.getMessage()),
                        e);
            }
            return null;
        });
    }

    protected void doInit()
        throws IOException,
        JAXBException
    {
        long configCount = configService.count();
        if (configCount > 0)
        {
            logger.debug("Reuse existing authorization config from database...");
            try
            {
                // get first of the available configs into work
                configService.findAll()
                             .ifPresent(authorizationConfigs -> config = authorizationConfigs.get(0));

                // process the case when for some reason we have more than one config
                if (configCount > 1)
                {
                    logger.warn("Taking first of the total of " + configCount + " authorization configs...");
                }
            }
            catch (OSerializationException e)
            {
                config = null;

                logger.error("Unable to reuse existing authorization config", e);

                throw new BeanInitializationException("Unable to reuse existing authorization config", e);
            }
        }

        if (config == null)
        {
            logger.debug("Load authorization config from XML file...");

            parser = new GenericParser<>(AuthorizationConfig.class);
            config = parser.parse(getConfigurationResource().getURL());

            // when Configuration retrieved from XML file validation process appears
            // if we found in XML file privilege or role that already defined as build-in
            // (based on role/privilege name) we will throw runtime exception
            validateConfig(config);
        }

        saveConfig();
    }

    @Transactional
    public synchronized void saveConfig()
    {

        configService.deleteAll();
        config.setObjectId(null);

        try
        {
            configService.save(config);
        }
        catch (Exception e)
        {
            logger.error("Unable to save configuration: ", e);
        }
    }

    private void validateConfig(@NotNull AuthorizationConfig config)
        throws ConfigurationException
    {
        // check that embedded roles was not overridden
        throwIfNotEmpty(toIntersection(config.getRoles()
                                             .getRoles(),
                                       Arrays.asList(Roles.values()),
                                       o -> ((Role) o).getName()
                                                      .toUpperCase(),
                                       o -> ((Roles) o).name()
                                                       .toUpperCase()),
                        "Embedded roles overriding is forbidden: ");
    }

    /**
     * Calculates intersection of two sets that was created from two iterable sources with help of two name functions
     * respectively.
     */
    private Sets.SetView<String> toIntersection(@NotNull Iterable<?> first,
                                                @NotNull Iterable<?> second,
                                                @NotNull NameFunction firstNameFunction,
                                                @NotNull NameFunction secondNameFunction)
    {
        return Sets.intersection(collect(first, firstNameFunction), collect(second, secondNameFunction));
    }

    public synchronized Optional<AuthorizationConfig> getConfig()
    {
        logger.debug("Get config -> " + config);
        return Optional.ofNullable(config);
    }

    public synchronized void updateConfig(AuthorizationConfig config)
    {
        validateConfig(config);

        // this is enough because we will execute actual saving before this bean died
        // see method annotated with @PreDestroy
        this.config = config;

        logger.debug("Update config -> " + this.config);

        saveConfig();
    }

    private Resource getConfigurationResource()
        throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("authorization.config.xml",
                                                                      "etc/conf/strongbox-authorization.xml");
    }

    // used to receive String representation of any object to execute future comparisons based on that
    private interface NameFunction
    {

        String name(Object o);
    }

}
