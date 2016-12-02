package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Repository configuration used custom serialization mechanism so we decided to decouple repository configuration cache
 * management logic.
 *
 * @author Alex Oreshkevich
 */
@Component
public class ConfigurationCache
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);

    @Inject
    CacheManager cacheManager;

    private GenericParser<Configuration> parser;

    private Cache configurationCache;

    @PostConstruct
    public synchronized void init()
    {
        parser = new GenericParser<>(Configuration.class);

        configurationCache = cacheManager.getCache("configuration");
        if (configurationCache == null)
        {
            throw new RuntimeException("Unable to get configuration cache");
        }
    }

    public Configuration save(Configuration configuration)
    {
        if (configuration == null)
        {
            logger.warn("Unable to save NULL configuration.");
            return null;
        }

        if (configuration.getId() == null)
        {
            throw new IllegalArgumentException("Unable to save configuration into cache: id is null");
        }

        try
        {
            String value = parser.serialize(configuration);
            configurationCache.evict(configuration.getId());
            configurationCache.put(configuration.getId(), value);
        }
        catch (Exception e)
        {
            logger.error("Unable to save configuration to cache", e);
        }

        return configuration;
    }

    public Optional<Configuration> getConfiguration(String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        try
        {
            Cache.ValueWrapper valueWrapper = configurationCache.get(id);
            if (valueWrapper == null)
            {
                return Optional.empty();
            }

            String configuration = (String) valueWrapper.get();
            if (configuration == null)
            {
                return Optional.empty();
            }

            return Optional.ofNullable(parser.deserialize(configuration));
        }
        catch (Exception e)
        {
            logger.error("Unable to retrieve configuration from cache", e);
            return Optional.empty();
        }
    }

    public GenericParser<Configuration> getParser()
    {
        return parser;
    }
}
