package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.xml.StorageMapAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration extends ServerConfiguration
{

    @XmlElement
    private String version = "1.0";

    @XmlElement
    private String baseUrl = "http://localhost/";

    @XmlElement
    private int port = 48080;

    /**
     * The global proxy settings to use when no per-repository proxy settings have been defined.
     */
    @XmlElement(name = "proxy-configuration")
    private ProxyConfiguration proxyConfiguration;

    /**
     * K: storageId
     * V: storage
     */
    @XmlElement(name = "storages")
    @XmlJavaTypeAdapter(StorageMapAdapter.class)
    private Map<String, Storage> storages = new LinkedHashMap<>();

    @XmlElement(name = "rule")
    @XmlElementWrapper(name = "routing-rules")
    private Set<RoutingRule> routingRules = new LinkedHashSet<>();


    public Configuration()
    {
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public Map<String, Storage> getStorages()
    {
        return storages;
    }

    public void setStorages(Map<String, Storage> storages)
    {
        this.storages = storages;
    }

    public void addStorage(Storage storage)
    {
        storages.put(storage.getId(), storage);
    }

    public Storage getStorage(String storageId)
    {
        return storages.get(storageId);
    }

    public void removeStorage(Storage storage)
    {
        storages.remove(storage.getBasedir());
    }

    public Set<RoutingRule> getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(Set<RoutingRule> routingRules)
    {
        this.routingRules = routingRules;
    }

}
