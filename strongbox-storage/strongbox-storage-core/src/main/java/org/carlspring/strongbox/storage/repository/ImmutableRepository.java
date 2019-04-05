package org.carlspring.strongbox.storage.repository;

import static org.carlspring.strongbox.util.CustomStreamCollectors.toLinkedHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.json.MapValuesJsonSerializer;
import org.carlspring.strongbox.json.StringArrayToMapJsonDeserializer;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.MutableCustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class ImmutableRepository
        implements Repository
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

    ImmutableRepository()
    {

    }

    public ImmutableRepository(final Repository delegate)
    {
        this(delegate, null);
    }

    public ImmutableRepository(final Repository delegate,
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
        
        MutableRepository mutableRepository = (MutableRepository)delegate;
        this.proxyConfiguration = immuteProxyConfiguration(mutableRepository.getProxyConfiguration());
        this.remoteRepository = immuteRemoteRepository(mutableRepository.getRemoteRepository());
        this.httpConnectionPool = immuteHttpConnectionPool(mutableRepository.getHttpConnectionPool());
        this.customConfigurations = immuteCustomConfigurations(mutableRepository.getCustomConfigurations());
        this.repositoryConfiguration = immuteCustomRepositoryConfiguration(mutableRepository.getRepositoryConfiguration());
        
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
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toLinkedHashMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    private Storage immuteStorage(final Storage source)
    {
        return source != null ? new ImmutableStorage(source) : null;
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
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toLinkedHashMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getBasedir()
    {
        return basedir;
    }

    @Override
    public String getPolicy()
    {
        return policy;
    }

    @Override
    public String getImplementation()
    {
        return implementation;
    }

    @Override
    public String getLayout()
    {
        return layout;
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public boolean isSecured()
    {
        return secured;
    }

    @Override
    public String getStatus()
    {
        return status;
    }

    @Override
    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    @Override
    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    @Override
    public boolean allowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    @Override
    public boolean allowsDeployment()
    {
        return allowsDeployment;
    }

    @Override
    public boolean allowsRedeployment()
    {
        return allowsRedeployment;
    }

    @Override
    public boolean allowsDeletion()
    {
        return allowsDelete;
    }

    @Override
    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    @Override
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

    public RepositoryConfiguration getRepositoryConfiguration()
    {
        return repositoryConfiguration;
    }

    @Override
    public Set<String> getGroupRepositories()
    {
        return groupRepositories.keySet();
    }

    @Override
    public Set<String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators.keySet();
    }

    @Override
    public Storage getStorage()
    {
        return storage;
    }

    @Override
    public boolean isHostedRepository()
    {
        return RepositoryTypeEnum.HOSTED.getType().equals(type);
    }

    @Override
    public boolean isProxyRepository()
    {
        return RepositoryTypeEnum.PROXY.getType().equals(type);
    }

    @Override
    public boolean isGroupRepository()
    {
        return RepositoryTypeEnum.GROUP.getType().equals(type);
    }

    @Override
    public boolean isInService()
    {
        return RepositoryStatusEnum.IN_SERVICE.getStatus().equalsIgnoreCase(getStatus());
    }

    @Override
    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsSnapshots();
    }

    @Override
    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsReleases();
    }

}
