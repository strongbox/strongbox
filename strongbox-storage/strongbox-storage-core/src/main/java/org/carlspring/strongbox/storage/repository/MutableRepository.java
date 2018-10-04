package org.carlspring.strongbox.storage.repository;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.providers.datastore.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.xml.ArtifactCoordinateValidatorsAdapter;
import org.carlspring.strongbox.xml.RepositoryGroupsAdapter;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repository")
public class MutableRepository
        implements Serializable
{

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute
    private String basedir;

    @XmlAttribute
    private String policy = RepositoryPolicyEnum.MIXED.getPolicy();

    @XmlAttribute
    private String implementation = FileSystemStorageProvider.ALIAS;

    @XmlAttribute
    private String layout;

    @XmlAttribute
    private String type = RepositoryTypeEnum.HOSTED.getType();

    @XmlAttribute
    private boolean secured;

    @XmlAttribute
    private String status = RepositoryStatusEnum.IN_SERVICE.getStatus();

    @XmlAttribute(name = "artifact-max-size")
    private long artifactMaxSize;

    @XmlAttribute(name = "trash-enabled")
    private boolean trashEnabled;

    @XmlAttribute(name = "allows-force-deletion")
    private boolean allowsForceDeletion;

    @XmlAttribute(name = "allows-deployment")
    private boolean allowsDeployment = true;

    @XmlAttribute(name = "allows-redeployment")
    private boolean allowsRedeployment = true;

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
    private MutableProxyConfiguration proxyConfiguration;

    @XmlElement(name = "remote-repository")
    private MutableRemoteRepository remoteRepository;

    @XmlElement(name = "http-connection-pool")
    private MutableHttpConnectionPool httpConnectionPool;

    @XmlElementRef
    private List<MutableCustomConfiguration> customConfigurations = new ArrayList<>();

    @XmlElementRef
    @JsonProperty("repositoryConfiguration")
    private MutableCustomRepositoryConfiguration repositoryConfiguration;

    @XmlElement(name = "group")
    @XmlJavaTypeAdapter(RepositoryGroupsAdapter.class)
    private Map<String, String> groupRepositories = new LinkedHashMap<>();

    @XmlElement(name = "artifact-coordinate-validators")
    @XmlJavaTypeAdapter(ArtifactCoordinateValidatorsAdapter.class)
    private Map<String, String> artifactCoordinateValidators = new LinkedHashMap<>();

    @XmlTransient
    private MutableStorage storage;


    public MutableRepository()
    {
    }

    public MutableRepository(String id)
    {
        this.id = id;
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
            return Paths.get(storage.getBasedir()).resolve(id).toString();
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

    public MutableProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(MutableProxyConfiguration proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public MutableRemoteRepository getRemoteRepository()
    {
        return remoteRepository;
    }

    public void setRemoteRepository(MutableRemoteRepository remoteRepository)
    {
        this.remoteRepository = remoteRepository;
    }

    public Map<String, String> getGroupRepositories()
    {
        return groupRepositories;
    }

    public void setGroupRepositories(Set<String> groupRepositories)
    {
        this.groupRepositories.clear();
        if (groupRepositories != null)
        {
            groupRepositories.stream().forEach(value -> this.groupRepositories.put(value, value));
        }
    }

    public void addRepositoryToGroup(String repositoryId)
    {
        groupRepositories.putIfAbsent(repositoryId, repositoryId);
    }

    public void removeRepositoryFromGroup(String repositoryId)
    {
        groupRepositories.remove(repositoryId);
    }

    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsSnapshots();
    }

    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsReleases();
    }

    public MutableStorage getStorage()
    {
        return storage;
    }

    public void setStorage(MutableStorage storage)
    {
        this.storage = storage;
    }

    @Override
    public String toString()
    {
        return id;
    }

    public MutableHttpConnectionPool getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public void setHttpConnectionPool(MutableHttpConnectionPool httpConnectionPool)
    {
        this.httpConnectionPool = httpConnectionPool;
    }

    public List<MutableCustomConfiguration> getCustomConfigurations()
    {
        return customConfigurations;
    }

    public void setCustomConfigurations(List<MutableCustomConfiguration> customConfigurations)
    {
        this.customConfigurations = customConfigurations;
    }

    public MutableCustomRepositoryConfiguration getRepositoryConfiguration()
    {
        return repositoryConfiguration;
    }

    public void setRepositoryConfiguration(MutableCustomRepositoryConfiguration repositoryConfiguration)
    {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public void setAllowsForceDeletion(boolean allowsForceDeletion)
    {
        this.allowsForceDeletion = allowsForceDeletion;
    }

    public void setAllowsDeployment(boolean allowsDeployment)
    {
        this.allowsDeployment = allowsDeployment;
    }

    public void setAllowsRedeployment(boolean allowsRedeployment)
    {
        this.allowsRedeployment = allowsRedeployment;
    }

    public void setAllowsDelete(boolean allowsDelete)
    {
        this.allowsDelete = allowsDelete;
    }

    public void setAllowsDirectoryBrowsing(boolean allowsDirectoryBrowsing)
    {
        this.allowsDirectoryBrowsing = allowsDirectoryBrowsing;
    }

    public boolean isHostedRepository()
    {
        return RepositoryTypeEnum.HOSTED.getType().equals(getType());
    }

    public boolean isProxyRepository()
    {
        return RepositoryTypeEnum.PROXY.getType().equals(getType());
    }

    public boolean isGroupRepository()
    {
        return RepositoryTypeEnum.GROUP.getType().equals(getType());
    }

    public boolean isVirtualRepository()
    {
        return RepositoryTypeEnum.VIRTUAL.getType().equals(getType());
    }

    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    public void setArtifactMaxSize(long artifactMaxSize)
    {
        this.artifactMaxSize = artifactMaxSize;
    }

    public Map<String, String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators;
    }

    public void setArtifactCoordinateValidators(Set<String> artifactCoordinateValidators)
    {
        this.artifactCoordinateValidators.clear();
        if (artifactCoordinateValidators != null)
        {
            artifactCoordinateValidators.stream().forEach(value -> this.artifactCoordinateValidators.put(value, value));
        }
    }

    public boolean hasRemoteRepositoryUrl()
    {
        return this.getHttpConnectionPool() != null &&
               this.getRemoteRepository() != null &&
               this.getRemoteRepository().getUrl() != null;
    }

}
