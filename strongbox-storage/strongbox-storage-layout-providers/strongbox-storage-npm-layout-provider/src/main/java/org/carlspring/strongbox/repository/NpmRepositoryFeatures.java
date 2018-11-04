package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.config.NpmLayoutProviderConfig.NpmObjectMapper;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.npm.NpmSearchRequest;
import org.carlspring.strongbox.npm.metadata.Change;
import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.npm.metadata.Versions;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericSnapshotVersionValidator;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableNpmRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.remote.NpmRemoteRepositoryConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

@Component
public class NpmRepositoryFeatures implements RepositoryFeatures
{

    private static final int CHANGES_BATCH_SIZE = 500;

    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryFeatures.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RedeploymentValidator redeploymentValidator;

    @Inject
    private GenericReleaseVersionValidator genericReleaseVersionValidator;

    @Inject
    private GenericSnapshotVersionValidator genericSnapshotVersionValidator;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private RepositoryPathLock repositoryPathLock;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Executor eventTaskExecutor;

    @Inject
    @NpmObjectMapper
    private ObjectMapper npmJacksonMapper;

    @Inject
    private PlatformTransactionManager transactionManager;

    private Set<String> defaultArtifactCoordinateValidators;

    @PostConstruct
    public void init()
    {
        defaultArtifactCoordinateValidators = new LinkedHashSet<>(Arrays.asList(redeploymentValidator.getAlias(),
                                                                                genericReleaseVersionValidator.getAlias(),
                                                                                genericSnapshotVersionValidator.getAlias()));
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return defaultArtifactCoordinateValidators;
    }

    public void fetchRemoteChangesFeed(String storageId,
                                       String repositoryId)
        throws IOException
    {

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository == null)
        {
            return;
        }

        MutableRepository mutableRepository = configurationManagementService.getMutableConfigurationClone()
                                                                            .getStorage(storageId)
                                                                            .getRepository(repositoryId);
        MutableNpmRemoteRepositoryConfiguration mutableConfiguration = (MutableNpmRemoteRepositoryConfiguration) mutableRepository.getRemoteRepository()
                                                                                                                                  .getCustomConfiguration();

        NpmRemoteRepositoryConfiguration configuration = (NpmRemoteRepositoryConfiguration) remoteRepository.getCustomConfiguration();
        if (configuration == null)
        {
            logger.warn(String.format("Remote npm configuration not found for [%s]/[%s]", storageId, repositoryId));
            return;
        }
        Long lastCnahgeId = configuration.getLastChangeId();
        String replicateUrl = configuration.getReplicateUrl();

        Long nextChangeId = lastCnahgeId;
        do
        {
            lastCnahgeId = nextChangeId;
            mutableConfiguration.setLastChangeId(nextChangeId);
            configurationManagementService.saveRepository(storageId, mutableRepository);
            
            nextChangeId = Long.valueOf(fetchRemoteChangesFeed(repository, replicateUrl, lastCnahgeId + 1));
        } while (nextChangeId > lastCnahgeId);
    }

    private Integer fetchRemoteChangesFeed(Repository repository,
                                           String replicateUrl,
                                           Long since)
        throws IOException
    {
        int result = 0;
        Client restClient = proxyRepositoryConnectionPoolConfigurationService.getRestClient();
        try
        {
            logger.debug(String.format("Fetching remote cnages for [%s] since [%s].", replicateUrl, since));

            WebTarget service = restClient.target(replicateUrl);
            service = service.path("_changes");
            service = service.queryParam("since", since);
            service = service.queryParam("include_docs", true);
            service = service.queryParam("limit", CHANGES_BATCH_SIZE);

            Invocation request = service.request().buildGet();

            result = fetchRemoteChangesFeed(repository, request);
        } finally
        {
            restClient.close();
        }

        return result;
    }

    private int fetchRemoteChangesFeed(Repository repository,
                                       Invocation request)
        throws IOException
    {
        int result = 0;

        RemoteRepository remoteRepository = repository.getRemoteRepository();
        NpmRemoteRepositoryConfiguration repositoryConfiguration = (NpmRemoteRepositoryConfiguration) remoteRepository.getCustomConfiguration();

        JsonFactory jfactory = new JsonFactory();

        try (InputStream is = request.invoke(InputStream.class))
        {

            JsonParser jp = jfactory.createParser(is);
            jp.setCodec(npmJacksonMapper);

            Assert.isTrue(jp.nextToken() == JsonToken.START_OBJECT, "npm changes feed should be JSON object.");
            Assert.isTrue(jp.nextFieldName().equals("results"), "npm changes feed should contains `results` field.");
            Assert.isTrue(jp.nextToken() == JsonToken.START_ARRAY, "npm changes feed `results` should be array.");

            StringBuffer sb = new StringBuffer();
            while (jp.nextToken() != null)
            {
                JsonToken nextToken = jp.currentToken();
                if (nextToken == JsonToken.END_ARRAY)
                {
                    break;
                }

                JsonNode node = jp.readValueAsTree();
                sb.append(node.toString());

                String changeValue = sb.toString();

                Change change;
                try
                {
                    change = npmJacksonMapper.readValue(changeValue, Change.class);
                    parseFeed(repository, change.getDoc());
                }
                catch (Exception e)
                {
                    logger.error(String.format("Failed to parse NPM cnahges feed [%s] since [%s]: %n %s",
                                               repositoryConfiguration.getReplicateUrl(),
                                               repositoryConfiguration.getLastChangeId(),
                                               changeValue),
                                 e);

                    return result;
                }

                result = change.getSeq();
                sb = new StringBuffer();
            }

        }

        logger.debug(String.format("Fetched remote cnages for  [%s] since [%s].",
                                   repositoryConfiguration.getReplicateUrl(),
                                   repositoryConfiguration.getLastChangeId()));

        return result;
    }

    public void downloadRemotePackageFeed(String storageId,
                                          String repositoryId,
                                          String packageId)
    {

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository == null)
        {
            return;
        }
        String remoteRepositoryUrl = remoteRepository.getUrl();

        PackageFeed packageFeed;
        Client restClient = proxyRepositoryConnectionPoolConfigurationService.getRestClient();
        try
        {
            logger.debug(String.format("Downloading NPM changes feed for [%s].", remoteRepositoryUrl));

            WebTarget service = restClient.target(remoteRepository.getUrl());
            service = service.path(packageId);

            InputStream inputStream = service.request().buildGet().invoke(InputStream.class);
            packageFeed = npmJacksonMapper.readValue(inputStream, PackageFeed.class);

            logger.debug(String.format("Downloaded NPM changes feed for [%s].", remoteRepository.getUrl()));

        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to fetch NPM changes feed [%s]", remoteRepositoryUrl), e);
            return;
        } finally
        {
            restClient.close();
        }

        parseFeed(repository, packageFeed);
    }

    private void parseFeed(Repository repository,
                           PackageFeed packageFeed)
    {
        try
        {
            parseFeedTransactional(repository, packageFeed);
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to parse NPM feed [%s/%s]", repository.getRemoteRepository().getUrl(),
                                       packageFeed.getName()),
                         e);
        }
    }

    private void parseFeedTransactional(Repository repository,
                                        PackageFeed packageFeed)
    {
        new TransactionTemplate(transactionManager).execute((s) -> {

            try
            {
                doParseFeed(repository, packageFeed);
            }
            catch (IOException e)
            {
                throw new UndeclaredThrowableException(e);
            }

            return null;
        });
    }

    private void doParseFeed(Repository repository,
                             PackageFeed packageFeed)
        throws IOException
    {
        if (packageFeed == null)
        {
            return;
        }

        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        Versions versions = packageFeed.getVersions();
        if (versions == null)
        {
            return;
        }

        Map<String, PackageVersion> versionMap = versions.getAdditionalProperties();
        if (versionMap == null || versionMap.isEmpty())
        {
            return;
        }

        Set<ArtifactEntry> artifactToSaveSet = new HashSet<>();
        for (PackageVersion packageVersion : versionMap.values())
        {
            RemoteArtifactEntry remoteArtifactEntry = parseVersion(storageId, repositoryId, packageVersion);
            if (remoteArtifactEntry == null)
            {
                continue;
            }

            if (packageVersion.getVersion().equals(packageFeed.getDistTags().getLatest()))
            {
                remoteArtifactEntry.getTagSet().add(lastVersionTag);
            }

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        for (ArtifactEntry e : artifactToSaveSet)
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(e);

            saveArtifactEntry(repositoryPath);
        }
    }

    private void saveArtifactEntry(RepositoryPath repositoryPath)
        throws IOException
    {
        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        ArtifactEntry e = repositoryPath.getArtifactEntry();

        Lock lock = repositoryPathLock.lock(repositoryPath).writeLock();
        lock.lock();

        try
        {
            if (artifactEntryService.artifactExists(e.getStorageId(), e.getRepositoryId(),
                                                    e.getArtifactCoordinates().toPath()))
            {
                return;
            }

            if (e.getTagSet().contains(lastVersionTag))
            {
                artifactEntryService.save(e, true);
            }
            else
            {
                artifactEntryService.save(e, false);
            }
        } finally
        {
            lock.unlock();
        }
    }

    private RemoteArtifactEntry parseVersion(String storageId,
                                             String repositoryId,
                                             PackageVersion packageVersion)
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageVersion.getName(), packageVersion.getVersion());

        RemoteArtifactEntry remoteArtifactEntry = new RemoteArtifactEntry();
        remoteArtifactEntry.setStorageId(storageId);
        remoteArtifactEntry.setRepositoryId(repositoryId);
        remoteArtifactEntry.setArtifactCoordinates(c);
        remoteArtifactEntry.setLastUsed(new Date());
        remoteArtifactEntry.setLastUpdated(new Date());
        remoteArtifactEntry.setDownloadCount(0);

        // TODO HEAD request for `tarball` URL ???
        // remoteArtifactEntry.setSizeInBytes(packageVersion.getProperties().getPackageSize());

        return remoteArtifactEntry;
    }

    @Component
    @Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public class RepositorySearchEventListener
    {

        private NpmSearchRequest npmSearchRequest;

        public NpmSearchRequest getNpmSearchRequest()
        {
            return npmSearchRequest;
        }

        public void setNpmSearchRequest(NpmSearchRequest npmSearchRequest)
        {
            this.npmSearchRequest = npmSearchRequest;
        }

        @EventListener
        public void handle(RemoteRepositorySearchEvent event)
        {
            if (npmSearchRequest == null)
            {
                return;
            }

            Storage storage = getConfiguration().getStorage(event.getSorageId());
            Repository repository = storage.getRepository(event.getRepositoryId());
            RemoteRepository remoteRepository = repository.getRemoteRepository();
            if (remoteRepository == null)
            {
                return;
            }

            Selector<RemoteArtifactEntry> selector = new Selector<>(RemoteArtifactEntry.class);
            selector.select("count(*)");
            selector.where(Predicate.of(ExpOperator.EQ.of("storageId", event.getSorageId())))
                    .and(Predicate.of(ExpOperator.EQ.of("repositoryId", event.getRepositoryId())));
            if (!event.getPredicate().isEmpty())
            {
                selector.getPredicate().and(event.getPredicate());
            }
            OQueryTemplate<Long, RemoteArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
            Long packageCount = queryTemplate.select(selector);

            logger.debug(String.format("NPM remote repository [%s] cached package count is [%s]", repository.getId(),
                                       packageCount));

            Runnable job = () -> downloadRemotePackageFeed(storage.getId(), repository.getId(),
                                                           npmSearchRequest.getPackageId());
            if (packageCount.longValue() == 0)
            {
                // Syncronously fetch remote package feed if ve have no cached
                // packages
                job.run();
            }
            else
            {
                eventTaskExecutor.execute(job);
            }
        }
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
