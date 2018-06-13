package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.carlspring.strongbox.xml.StorageMapAdapter;

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
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableConfiguration
        extends ServerConfiguration
{

    @XmlElement(name = "instance-name")
    private String instanceName = "strongbox";

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
    private MutableProxyConfiguration proxyConfiguration;

    @XmlElement(name = "session-configuration")
    private MutableSessionConfiguration sessionConfiguration;

    @XmlElement(name = "remote-repositories-configuration")
    private MutableRemoteRepositoriesConfiguration remoteRepositoriesConfiguration = MutableRemoteRepositoriesConfiguration.DEFAULT;

    /**
     * K: storageId
     * V: storage
     */
    @XmlElement(name = "storages")
    @XmlJavaTypeAdapter(StorageMapAdapter.class)
    private Map<String, MutableStorage> storages = new LinkedHashMap<>();

    @XmlElement(name = "routing-rules")
    private MutableRoutingRules routingRules = new MutableRoutingRules();


    public MutableConfiguration()
    {
    }

    public String getInstanceName()
    {
        return instanceName;
    }

    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
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

    public MutableProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(MutableProxyConfiguration proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public MutableSessionConfiguration getSessionConfiguration()
    {
        return sessionConfiguration;
    }

    public void setSessionConfiguration(MutableSessionConfiguration sessionConfiguration)
    {
        this.sessionConfiguration = sessionConfiguration;
    }

    public Map<String, MutableStorage> getStorages()
    {
        return storages;
    }

    public void setStorages(Map<String, MutableStorage> storages)
    {
        this.storages = storages;
    }

    public void addStorage(MutableStorage storage)
    {
        String key = storage.getId();
        if (key == null || key.isEmpty())
        {
            throw new IllegalArgumentException("Null keys are not supported!");
        }

        storages.put(key, storage);
    }

    public MutableStorage getStorage(String storageId)
    {
        return storages.get(storageId);
    }

    public void removeStorage(MutableStorage storage)
    {
        storages.remove(storage.getBasedir());
    }

    public MutableRoutingRules getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(MutableRoutingRules routingRules)
    {
        this.routingRules = routingRules;
    }

    public MutableRemoteRepositoriesConfiguration getRemoteRepositoriesConfiguration()
    {
        return remoteRepositoriesConfiguration;
    }

    public void setRemoteRepositoriesConfiguration(MutableRemoteRepositoriesConfiguration remoteRepositoriesConfiguration)
    {
        this.remoteRepositoriesConfiguration = remoteRepositoriesConfiguration;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableConfiguration that = (MutableConfiguration) o;
        return port == that.port &&
               Objects.equal(instanceName, that.instanceName) &&
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
                          .add("\n\tinstanceName", instanceName)
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
