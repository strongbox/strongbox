package org.carlspring.strongbox.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.springframework.core.io.Resource;

import org.carlspring.strongbox.storage.Storage;

import java.util.*;

/**
 * @author mtodorov
 */
@XStreamAlias("configuration")
public class Configuration
{

    @XStreamAlias(value = "version")
    private String version = "1.0";

    @XStreamAlias(value = "baseUrl")
    private String baseUrl = "http://localhost/";

    @XStreamAlias(value = "port")
    private int port = 48080;

    /**
     * The global proxy settings to use when no per-repository proxy settings have been defined.
     */
    @XStreamAlias("proxy-configuration")
    private ProxyConfiguration proxyConfiguration;

    @XStreamAlias(value = "storages")
    private Map<String, Storage> storages = new LinkedHashMap<String, Storage>();

    @XStreamOmitField
    private Resource resource;


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
        storages.put(storage.getBasedir(), storage);
    }

    public void removeStorage(Storage storage)
    {
        storages.remove(storage.getBasedir());
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(Resource resource)
    {
        this.resource = resource;
    }

}
