package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.xml.StorageMapAdapter;

import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration
        extends ServerConfiguration
{

    @XmlElement
    private String version = "1.0";

    @XmlElement
    private String revision;

    @XmlElement
    private String baseUrl = "http://localhost/";

    @XmlElement
    private int port = 48080;

    /**
     * The global proxy settings to use when no per-repository proxy settings have been defined.
     */
    @XmlElement(name = "proxy-configuration")
    private ProxyConfiguration proxyConfiguration;

    @XmlElement(name = "session-configuration")
    private SessionConfiguration sessionConfiguration;

    @XmlElement(name = "remote-repositories-configuration")
    private RemoteRepositoriesConfiguration remoteRepositoriesConfiguration = RemoteRepositoriesConfiguration.DEFAULT;

    /**
     * K: storageId
     * V: storage
     */
    @XmlElement(name = "storages")
    @XmlJavaTypeAdapter(StorageMapAdapter.class)
    @Embedded
    private Map<String, Storage> storages = new LinkedHashMap<>();

    @XmlElement(name = "routing-rules")
    private RoutingRules routingRules = new RoutingRules();


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

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
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

    public SessionConfiguration getSessionConfiguration()
    {
        return sessionConfiguration;
    }

    public void setSessionConfiguration(SessionConfiguration sessionConfiguration)
    {
        this.sessionConfiguration = sessionConfiguration;
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
        String key = storage.getId();
        if (key == null || key.isEmpty())
        {
            throw new IllegalArgumentException("Null keys are not supported!");
        }

        storages.put(key, storage);
    }

    public Storage getStorage(String storageId)
    {
        return storages.get(storageId);
    }

    public void removeStorage(Storage storage)
    {
        storages.remove(storage.getBasedir());
    }

    public RoutingRules getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(RoutingRules routingRules)
    {
        this.routingRules = routingRules;
    }

    public RemoteRepositoriesConfiguration getRemoteRepositoriesConfiguration()
    {
        return remoteRepositoriesConfiguration;
    }

    public void setRemoteRepositoriesConfiguration(RemoteRepositoriesConfiguration remoteRepositoriesConfiguration)
    {
        this.remoteRepositoriesConfiguration = remoteRepositoriesConfiguration;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return port == that.port &&
               Objects.equal(version, that.version) &&
               Objects.equal(baseUrl, that.baseUrl) &&
               Objects.equal(proxyConfiguration, that.proxyConfiguration) &&
               Objects.equal(sessionConfiguration, that.sessionConfiguration) &&
               Objects.equal(storages, that.storages) &&
               Objects.equal(routingRules, that.routingRules) &&
               Objects.equal(remoteRepositoriesConfiguration, that.remoteRepositoriesConfiguration);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(version, baseUrl, port, proxyConfiguration, sessionConfiguration, storages,
                                routingRules, remoteRepositoriesConfiguration);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                          .add("\n\tversion", version)
                          .add("\n\tbaseUrl", baseUrl)
                          .add("\n\tport", port)
                          .add("\n\tproxyConfiguration", proxyConfiguration)
                          .add("\n\tsessionConfiguration", sessionConfiguration)
                          .add("\n\tstorages", storages)
                          .add("\n\troutingRules", routingRules)
                          .add("\n\tremoteRepositoriesConfiguration", remoteRepositoriesConfiguration)
                          .toString();
    }

}
