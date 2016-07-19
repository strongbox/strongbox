package org.carlspring.strongbox.storage.resolvers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class LocationResolverRegistry
{

    private static final Logger logger = LoggerFactory.getLogger(LocationResolverRegistry.class);

    /**
     * K: Alias
     * V: Resolver implementation
     */
    private Map<String, LocationResolver> resolvers = new LinkedHashMap<>();


    public LocationResolverRegistry()
    {
    }

    /*
    public void listResolvers()
    {
        logger.info("Loading resolvers...");

        for (String key : getResolvers().keySet())
        {
            LocationResolver resolver = getResolvers().get(key);
            logger.info(" -> " + resolver.getClass());
        }
    }
    */

    public Map<String, LocationResolver> getResolvers()
    {
        return resolvers;
    }

    public void setResolvers(Map<String, LocationResolver> resolvers)
    {
        this.resolvers = resolvers;
    }

    public LocationResolver getResolver(String alias)
    {
        return resolvers.get(alias);
    }

    public LocationResolver addResolver(String alias,
                                        LocationResolver resolver)
    {
        return resolvers.put(alias, resolver);
    }

    public LocationResolver removeResolver(String alias)
    {
        return resolvers.remove(alias);
    }

}
