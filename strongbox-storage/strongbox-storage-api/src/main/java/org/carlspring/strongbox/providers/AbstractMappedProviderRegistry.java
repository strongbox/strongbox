package org.carlspring.strongbox.providers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author carlspring
 */
public abstract class AbstractMappedProviderRegistry<T>
{

    /**
     * K: The alias
     * V: The provider implementation
     */
    private Map<String, T> providers = new LinkedHashMap<>();


    public AbstractMappedProviderRegistry()
    {
    }

    public abstract void initialize();

    public Map<String, T> getProviders()
    {
        return providers;
    }

    public void setProviders(Map<String, T> providers)
    {
        this.providers = providers;
    }

    public T getProvider(String alias)
    {
        return providers.get(alias);
    }

    public T addProvider(String alias,
                         T provider)
    {
        return providers.put(alias, provider);
    }

    public void removeProvider(String alias)
    {
        providers.remove(alias);
    }

}
