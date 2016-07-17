package org.carlspring.strongbox.storage.resolvers;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author carlspring
 */
@Component
@Deprecated
public class LocationResolverRegistry
{

    /**
     * K: Alias
     * V: Resolver implementation
     */
    private Map<String, LocationResolver> resolvers = new LinkedHashMap<>();


    public LocationResolverRegistry()
    {
    }

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

    public LocationResolver addResolver(String alias, LocationResolver resolver)
    {
        return resolvers.put(alias, resolver);
    }

    public LocationResolver removeResolver(String alias)
    {
        return resolvers.remove(alias);
    }

}
