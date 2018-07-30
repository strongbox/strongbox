package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.validation.configuration.DescribableEnumValue;
import org.carlspring.strongbox.validation.configuration.LayoutProviderValue;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryForm
{

    @NotEmpty(message = "An id must be specified.")
    private String id;

    private String basedir;

    @NotEmpty(message = "A policy must be specified.")
    @DescribableEnumValue(message = "A policy value is invalid.", type = RepositoryPolicyEnum.class)
    private String policy;

    @NotEmpty(message = "An implementation must be specified.")
    @DescribableEnumValue(message = "An implementation value is invalid.", type = StorageProviderEnum.class)
    private String implementation;

    @NotEmpty(message = "A layout must be specified.")
    @LayoutProviderValue(message = "A layout value is invalid.")
    private String layout;

    @NotEmpty(message = "A type must be specified.")
    @DescribableEnumValue(message = "A type value is invalid.", type = RepositoryTypeEnum.class)
    private String type;

    private boolean secured;

    @NotEmpty(message = "A status must be specified.")
    @DescribableEnumValue(message = "A status value is invalid.", type = RepositoryStatusEnum.class)
    private String status;

    private long artifactMaxSize;

    private boolean trashEnabled = true;

    private boolean allowsForceDeletion;

    private boolean allowsDeployment = true;

    private boolean allowsRedeployment;

    private boolean allowsDelete = true;

    private boolean allowsDirectoryBrowsing = true;

    private boolean checksumHeadersEnabled;

    @Valid
    private ProxyConfigurationForm proxyConfiguration;

    @Valid
    private RemoteRepositoryForm remoteRepository;

    @PositiveOrZero(message = "A httpConnectionPool must be positive or zero.")
    private Integer httpConnectionPool;

    private Set<String> groupRepositories;

    private Set<String> artifactCoordinateValidators;

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

    public ProxyConfigurationForm getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(final ProxyConfigurationForm proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public RemoteRepositoryForm getRemoteRepository()
    {
        return remoteRepository;
    }

    public void setRemoteRepository(final RemoteRepositoryForm remoteRepository)
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
