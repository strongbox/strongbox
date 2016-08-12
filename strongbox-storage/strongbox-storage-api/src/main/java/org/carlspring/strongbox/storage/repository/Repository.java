package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;

import javax.persistence.Version;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repository")
public class Repository
        implements Serializable
{

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;
    
    @Version
    @JsonIgnore
    protected Long version;
    
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String basedir;

    @XmlAttribute
    private String policy = RepositoryPolicyEnum.MIXED.getPolicy();

    @XmlAttribute
    private String implementation = "file-system";

    @XmlAttribute
    private String layout = RepositoryLayoutEnum.MAVEN_2.getLayout();

    @XmlAttribute
    private String type = RepositoryTypeEnum.HOSTED.getType();

    @XmlAttribute
    private boolean secured;

    @XmlAttribute
    private String status = RepositoryStatusEnum.IN_SERVICE.getStatus();

    @XmlAttribute(name = "trash-enabled")
    private boolean trashEnabled;

    @XmlAttribute(name = "indexing-enabled")
    private boolean indexingEnabled;

    @XmlAttribute(name = "allows-force-deletion")
    private boolean allowsForceDeletion;

    @XmlAttribute(name = "allows-deployment")
    private boolean allowsDeployment = true;

    @XmlAttribute(name = "allows-redeployment")
    private boolean allowsRedeployment;

    @XmlAttribute(name = "allows-delete")
    private boolean allowsDelete = true;

    @XmlAttribute(name = "allows-directory-browsing")
    private boolean allowsDirectoryBrowsing = true;

    @XmlAttribute(name = "checksum-headers-enabled")
    private boolean checksumHeadersEnabled;

    /**
     * The per-repository proxy settings that override the overall global proxy settings.
     */
    @XmlElement(name = "proxy-configuration")
    private ProxyConfiguration proxyConfiguration;

    @XmlElement(name = "remote-repository")
    private RemoteRepository remoteRepository;

    @XmlElement(name = "http-connection-pool")
    private HttpConnectionPool httpConnectionPool;

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "group")
    private Set<String> groupRepositories = new LinkedHashSet<>();

    @XmlTransient
    private Storage storage;


    public Repository()
    {
    }

    public Repository(String id)
    {
        this.id = id;
    }

    public Repository(String id,
                      boolean secured)
    {
        this.id = id;
        this.secured = secured;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getBasedir()
    {
        if (basedir != null)
        {
            return basedir;
        }
        else
        {
            return storage.getBasedir() + "/" + id;
        }
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public String getPolicy()
    {
        return policy;
    }

    public void setPolicy(String policy)
    {
        this.policy = policy;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public String getLayout()
    {
        return layout;
    }

    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isSecured()
    {
        return secured;
    }

    public void setSecured(boolean secured)
    {
        this.secured = secured;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public boolean isInService()
    {
        return RepositoryStatusEnum.IN_SERVICE.getStatus().equalsIgnoreCase(getStatus());
    }

    public void putInService()
    {
        status = RepositoryStatusEnum.IN_SERVICE.getStatus();
    }

    public void putOutOfService()
    {
        status = RepositoryStatusEnum.OUT_OF_SERVICE.getStatus();
    }

    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    public void setTrashEnabled(boolean trashEnabled)
    {
        this.trashEnabled = trashEnabled;
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled)
    {
        this.indexingEnabled = indexingEnabled;
    }

    public boolean allowsDeletion()
    {
        return allowsDelete;
    }

    public boolean allowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    public boolean allowsDeployment()
    {
        return allowsDeployment;
    }

    public boolean allowsRedeployment()
    {
        return allowsRedeployment;
    }

    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public boolean isChecksumHeadersEnabled()
    {
        return checksumHeadersEnabled;
    }

    public void setChecksumHeadersEnabled(boolean checksumHeadersEnabled)
    {
        this.checksumHeadersEnabled = checksumHeadersEnabled;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public RemoteRepository getRemoteRepository()
    {
        return remoteRepository;
    }

    public void setRemoteRepository(RemoteRepository remoteRepository)
    {
        this.remoteRepository = remoteRepository;
    }

    public Set<String> getGroupRepositories()
    {
        return groupRepositories;
    }

    public void setGroupRepositories(Set<String> groupRepositories)
    {
        this.groupRepositories = groupRepositories;
    }

    public void addRepositoryToGroup(String repositoryId)
    {
        groupRepositories.add(repositoryId);
    }

    public void removeRepositoryFromGroup(String repositoryId)
    {
        groupRepositories.remove(repositoryId);
    }

    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.SNAPSHOT.toString().equals(getPolicy());
    }

    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.RELEASE.toString().equals(getPolicy());
    }

    public Storage getStorage()
    {
        return storage;
    }

    public void setStorage(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public String toString()
    {
        return id;
    }

    public File getTrashDir()
    {
        return new File(getBasedir(), ".trash");
    }

    public File getTempDir()
    {
        return new File(getBasedir(), ".temp");
    }

    public HttpConnectionPool getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public void setHttpConnectionPool(HttpConnectionPool httpConnectionPool)
    {
        this.httpConnectionPool = httpConnectionPool;
    }

    public String getDetachAll()
    {
        return detachAll;
    }

    public void setDetachAll(String detachAll)
    {
        this.detachAll = detachAll;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public boolean isAllowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    public void setAllowsForceDeletion(boolean allowsForceDeletion)
    {
        this.allowsForceDeletion = allowsForceDeletion;
    }

    public boolean isAllowsDeployment()
    {
        return allowsDeployment;
    }

    public void setAllowsDeployment(boolean allowsDeployment)
    {
        this.allowsDeployment = allowsDeployment;
    }

    public boolean isAllowsRedeployment()
    {
        return allowsRedeployment;
    }

    public void setAllowsRedeployment(boolean allowsRedeployment)
    {
        this.allowsRedeployment = allowsRedeployment;
    }

    public boolean isAllowsDelete()
    {
        return allowsDelete;
    }

    public void setAllowsDelete(boolean allowsDelete)
    {
        this.allowsDelete = allowsDelete;
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public void setAllowsDirectoryBrowsing(boolean allowsDirectoryBrowsing)
    {
        this.allowsDirectoryBrowsing = allowsDirectoryBrowsing;
    }
    
}
