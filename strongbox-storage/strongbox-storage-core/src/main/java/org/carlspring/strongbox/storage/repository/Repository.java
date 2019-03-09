package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.json.MapValuesJsonSerializer;
import org.carlspring.strongbox.json.StringArrayToMapJsonDeserializer;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class Repository
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

    private ProxyConfiguration proxyConfiguration;

    private RemoteRepository remoteRepository;

    private HttpConnectionPool httpConnectionPool;

    private List<CustomConfiguration> customConfigurations;

    private CustomRepositoryConfiguration repositoryConfiguration;

    @JsonSerialize(using = MapValuesJsonSerializer.class)
    @JsonDeserialize(using = StringArrayToMapJsonDeserializer.class)
    private Map<String, String> groupRepositories;

    @JsonSerialize(using = MapValuesJsonSerializer.class)
    @JsonDeserialize(using = StringArrayToMapJsonDeserializer.class)
    private Map<String, String> artifactCoordinateValidators;

    @JsonIgnore
    private Storage storage;

    Repository()
    {

    }

    public Repository(final MutableRepository delegate)
    {
        this(delegate, null);
    }

    public Repository(final MutableRepository delegate,
                      final Storage storage)
    {
        this.id = delegate.getId();
        this.policy = delegate.getPolicy();
        this.implementation = delegate.getImplementation();
        this.layout = delegate.getLayout();
        this.type = delegate.getType();
        this.secured = delegate.isSecured();
        this.status = delegate.getStatus();
        this.artifactMaxSize = delegate.getArtifactMaxSize();
        this.trashEnabled = delegate.isTrashEnabled();
        this.allowsForceDeletion = delegate.allowsForceDeletion();
        this.allowsDeployment = delegate.allowsDeployment();
        this.allowsRedeployment = delegate.allowsRedeployment();
        this.allowsDelete = delegate.allowsDeletion();
        this.allowsDirectoryBrowsing = delegate.allowsDirectoryBrowsing();
        this.checksumHeadersEnabled = delegate.isChecksumHeadersEnabled();
        this.proxyConfiguration = immuteProxyConfiguration(delegate.getProxyConfiguration());
        this.remoteRepository = immuteRemoteRepository(delegate.getRemoteRepository());
        this.httpConnectionPool = immuteHttpConnectionPool(delegate.getHttpConnectionPool());
        this.customConfigurations = immuteCustomConfigurations(delegate.getCustomConfigurations());
        this.repositoryConfiguration = immuteCustomRepositoryConfiguration(delegate.getRepositoryConfiguration());
        this.groupRepositories = immuteGroupRepositories(delegate.getGroupRepositories());
        this.artifactCoordinateValidators = immuteArtifactCoordinateValidators(
                delegate.getArtifactCoordinateValidators());
        this.storage = storage != null ? storage : immuteStorage(delegate.getStorage());
        this.basedir = delegate.getBasedir();
    }

    private ProxyConfiguration immuteProxyConfiguration(final MutableProxyConfiguration source)
    {
        return source != null ? new ProxyConfiguration(source) : null;
    }

    private RemoteRepository immuteRemoteRepository(final MutableRemoteRepository source)
    {
        return source != null ? new RemoteRepository(source) : null;
    }

    private Map<String, String> immuteGroupRepositories(final Set<String> source)
    {
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    private Storage immuteStorage(final MutableStorage source)
    {
        return source != null ? new Storage(source) : null;
    }

    private HttpConnectionPool immuteHttpConnectionPool(final MutableHttpConnectionPool source)
    {
        return source != null ? new HttpConnectionPool(source) : null;
    }

    private List<CustomConfiguration> immuteCustomConfigurations(final List<MutableCustomConfiguration> source)
    {
        return source != null ?
               ImmutableList.copyOf(source.stream().map(MutableCustomConfiguration::getImmutable).collect(
                       Collectors.toList())) : Collections.emptyList();
    }

    private CustomRepositoryConfiguration immuteCustomRepositoryConfiguration(final MutableCustomRepositoryConfiguration source)
    {
        return source != null ? source.getImmutable() : null;
    }


    private Map<String, String> immuteArtifactCoordinateValidators(final Set<String> source)
    {
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    public String getId()
    {
        return id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public String getPolicy()
    {
        return policy;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public String getLayout()
    {
        return layout;
    }

    public String getType()
    {
        return type;
    }

    public boolean isSecured()
    {
        return secured;
    }

    public String getStatus()
    {
        return status;
    }

    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    public boolean isTrashEnabled()
    {
        return trashEnabled;
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

    public boolean allowsDeletion()
    {
        return allowsDelete;
    }

    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public boolean isChecksumHeadersEnabled()
    {
        return checksumHeadersEnabled;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public RemoteRepository getRemoteRepository()
    {
        return remoteRepository;
    }

    public HttpConnectionPool getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public List<CustomConfiguration> getCustomConfigurations()
    {
        return customConfigurations;
    }

    public CustomRepositoryConfiguration getRepositoryConfiguration()
    {
        return repositoryConfiguration;
    }

    public Map<String, String> getGroupRepositories()
    {
        return groupRepositories;
    }

    public Map<String, String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators;
    }

    public Storage getStorage()
    {
        return storage;
    }

    public boolean isHostedRepository()
    {
        return RepositoryTypeEnum.HOSTED.getType().equals(type);
    }

    public boolean isProxyRepository()
    {
        return RepositoryTypeEnum.PROXY.getType().equals(type);
    }

    public boolean isGroupRepository()
    {
        return RepositoryTypeEnum.GROUP.getType().equals(type);
    }

    public boolean isInService()
    {
        return RepositoryStatusEnum.IN_SERVICE.getStatus().equalsIgnoreCase(getStatus());
    }

    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsSnapshots();
    }

    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsReleases();
    }
}
