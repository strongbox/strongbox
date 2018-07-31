package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.storage.repository.MutableRepository;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.util.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@JsonRootName("repository")
public class RepositoryOutput
{

    private String id;

    private String basedir;

    private String policy;

    private String implementation;

    private String layout;

    private String type;

    private boolean secured;

    private String status;

    private long artifactMaxSize;

    private boolean trashEnabled;

    private boolean allowsForceDeletion;

    private boolean allowsDeployment;

    private boolean allowsRedeployment;

    private boolean allowsDelete;

    private boolean allowsDirectoryBrowsing;

    private boolean checksumHeadersEnabled;

    private ProxyConfigurationOutput proxyConfiguration;

    private RemoteRepositoryOutput remoteRepository;

    private Integer httpConnectionPool;

    private Set<String> groupRepositories;

    private Set<String> artifactCoordinateValidators;

    public RepositoryOutput()
    {
    }

    public RepositoryOutput(final MutableRepository repository)
    {
        this.id = repository.getId();
        this.basedir = repository.getBasedir();
        this.policy = repository.getPolicy();
        this.implementation = repository.getImplementation();
        this.layout = repository.getLayout();
        this.type = repository.getType();
        this.secured = repository.isSecured();
        this.status = repository.getStatus();
        this.artifactMaxSize = repository.getArtifactMaxSize();
        this.trashEnabled = repository.isTrashEnabled();
        this.allowsForceDeletion = repository.allowsForceDeletion();
        this.allowsDeployment = repository.allowsDeployment();
        this.allowsRedeployment = repository.allowsRedeployment();
        this.allowsDelete = repository.allowsDeletion();
        this.allowsDirectoryBrowsing = repository.allowsDirectoryBrowsing();
        this.checksumHeadersEnabled = repository.isChecksumHeadersEnabled();
        if (repository.getProxyConfiguration() != null)
        {
            // TODO this.proxyConfiguration = repository.proxyConfiguration;
        }
        if (repository.getRemoteRepository() != null)
        {
            // TODO this.httpConnectionPool = repository.remoteRepository;
        }
        if (repository.getHttpConnectionPool() != null)
        {
            // TODO this.httpConnectionPool = repository.httpConnectionPool;
        }
        if (!CollectionUtils.isEmpty(repository.getGroupRepositories()))
        {
            this.groupRepositories = repository.getGroupRepositories().keySet();
        }
        if (!CollectionUtils.isEmpty(repository.getArtifactCoordinateValidators()))
        {
            this.artifactCoordinateValidators = repository.getArtifactCoordinateValidators().keySet();
        }
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(final String basedir)
    {
        this.basedir = basedir;
    }

    public String getPolicy()
    {
        return policy;
    }

    public void setPolicy(final String policy)
    {
        this.policy = policy;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public void setImplementation(final String implementation)
    {
        this.implementation = implementation;
    }

    public String getLayout()
    {
        return layout;
    }

    public void setLayout(final String layout)
    {
        this.layout = layout;
    }

    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    public boolean isSecured()
    {
        return secured;
    }

    public void setSecured(final boolean secured)
    {
        this.secured = secured;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(final String status)
    {
        this.status = status;
    }

    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    public void setArtifactMaxSize(final long artifactMaxSize)
    {
        this.artifactMaxSize = artifactMaxSize;
    }

    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    public void setTrashEnabled(final boolean trashEnabled)
    {
        this.trashEnabled = trashEnabled;
    }

    public boolean isAllowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    public void setAllowsForceDeletion(final boolean allowsForceDeletion)
    {
        this.allowsForceDeletion = allowsForceDeletion;
    }

    public boolean isAllowsDeployment()
    {
        return allowsDeployment;
    }

    public void setAllowsDeployment(final boolean allowsDeployment)
    {
        this.allowsDeployment = allowsDeployment;
    }

    public boolean isAllowsRedeployment()
    {
        return allowsRedeployment;
    }

    public void setAllowsRedeployment(final boolean allowsRedeployment)
    {
        this.allowsRedeployment = allowsRedeployment;
    }

    public boolean isAllowsDelete()
    {
        return allowsDelete;
    }

    public void setAllowsDelete(final boolean allowsDelete)
    {
        this.allowsDelete = allowsDelete;
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public void setAllowsDirectoryBrowsing(final boolean allowsDirectoryBrowsing)
    {
        this.allowsDirectoryBrowsing = allowsDirectoryBrowsing;
    }

    public boolean isChecksumHeadersEnabled()
    {
        return checksumHeadersEnabled;
    }

    public void setChecksumHeadersEnabled(final boolean checksumHeadersEnabled)
    {
        this.checksumHeadersEnabled = checksumHeadersEnabled;
    }

    public ProxyConfigurationOutput getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(final ProxyConfigurationOutput proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public RemoteRepositoryOutput getRemoteRepository()
    {
        return remoteRepository;
    }

    public void setRemoteRepository(final RemoteRepositoryOutput remoteRepository)
    {
        this.remoteRepository = remoteRepository;
    }

    public Integer getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public void setHttpConnectionPool(final Integer httpConnectionPool)
    {
        this.httpConnectionPool = httpConnectionPool;
    }

    public Set<String> getGroupRepositories()
    {
        return groupRepositories;
    }

    public void setGroupRepositories(final Set<String> groupRepositories)
    {
        this.groupRepositories = groupRepositories;
    }

    public Set<String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators;
    }

    public void setArtifactCoordinateValidators(final Set<String> artifactCoordinateValidators)
    {
        this.artifactCoordinateValidators = artifactCoordinateValidators;
    }
}
