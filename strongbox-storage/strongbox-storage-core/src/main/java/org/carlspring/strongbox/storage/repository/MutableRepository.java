package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.providers.datastore.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MutableRepository
        implements Serializable
{
    private String id;

    private String basedir;

    private String policy = RepositoryPolicyEnum.MIXED.getPolicy();

    @JsonProperty("dataStore")
    private String implementation = FileSystemStorageProvider.ALIAS;

    private String layout;

    private String type = RepositoryTypeEnum.HOSTED.getType();

    private boolean secured;

    private String status = RepositoryStatusEnum.IN_SERVICE.getStatus();

    private long artifactMaxSize;

    private boolean trashEnabled;

    private boolean allowsForceDeletion;

    private boolean allowsDeployment = true;

    private boolean allowsRedeployment = true;

    private boolean allowsDelete = true;

    private boolean allowsDirectoryBrowsing = true;

    private boolean checksumHeadersEnabled;

    /**
     * The per-repository proxy settings that override the overall global proxy settings.
     */
    private MutableProxyConfiguration proxyConfiguration;

    private MutableRemoteRepository remoteRepository;

    private MutableHttpConnectionPool httpConnectionPool;

    private List<MutableCustomConfiguration> customConfigurations = new ArrayList<>();

    private MutableCustomRepositoryConfiguration repositoryConfiguration;

    private Set<String> groupRepositories = new LinkedHashSet<>();

    private Set<String> artifactCoordinateValidators = new LinkedHashSet<>();

    @JsonIgnore
    private MutableStorage storage;

    public MutableRepository()
    {
    }

    @JsonCreator
    public MutableRepository(@JsonProperty(value = "id", required = true) String id)
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
        else if (storage != null)
        {
            return Paths.get(storage.getBasedir()).resolve(id).toString();
        }
        else
        {
            return null;
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

    public Set<String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators;
    }

    public void setArtifactCoordinateValidators(Set<String> artifactCoordinateValidators)
    {
        this.artifactCoordinateValidators = artifactCoordinateValidators;
    }

    public boolean isEligibleForCustomConnectionPool()
    {
        return this.getHttpConnectionPool() != null &&
               this.getRemoteRepository() != null &&
               this.getRemoteRepository().getUrl() != null;
    }

}
