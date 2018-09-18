package org.carlspring.strongbox.repository;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
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
import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericSnapshotVersionValidator;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NpmRepositoryFeatures implements RepositoryFeatures
{

    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryFeatures.class);

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
            logger.debug(String.format("Downloading remote feed for [%s].", remoteRepositoryUrl));

            WebTarget service = restClient.target(remoteRepository.getUrl());
            service = service.path(packageId);

            packageFeed = service.request().buildGet().invoke(PackageFeed.class);

            logger.debug(String.format("Downloaded remote feed for [%s].", remoteRepository.getUrl()));

        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to fetch Nuget remote feed [%s]", remoteRepositoryUrl), e);
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
        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        Set<ArtifactEntry> artifactToSaveSet = new HashSet<>();
        for (PackageVersion packageVersion : packageFeed.getVersions().getAdditionalProperties().values())
        {
            NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageFeed.getName(), packageVersion.getVersion());
            if (artifactEntryService.artifactExists(storageId, repositoryId, c.toPath()))
            {
                continue;
            }

            RemoteArtifactEntry remoteArtifactEntry = new RemoteArtifactEntry();
            remoteArtifactEntry.setStorageId(storageId);
            remoteArtifactEntry.setRepositoryId(repositoryId);
            remoteArtifactEntry.setArtifactCoordinates(c);
            remoteArtifactEntry.setLastUsed(new Date());
            remoteArtifactEntry.setLastUpdated(new Date());
            remoteArtifactEntry.setDownloadCount(0);

            // TODO HEAD request for `tarball` URL ???
            // remoteArtifactEntry.setSizeInBytes(packageVersion.getProperties().getPackageSize());

            if (packageVersion.getVersion().equals(packageFeed.getDistTags().getLatest()))
            {
                remoteArtifactEntry.getTagSet().add(lastVersionTag);
            }

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        for (ArtifactEntry e : artifactToSaveSet)
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, e.getArtifactCoordinates());

            Lock lock = repositoryPathLock.lock(repositoryPath).writeLock();
            lock.lock();

            try
            {
                if (e.getTagSet().contains(lastVersionTag))
                {
                    artifactEntryService.save(e, true);
                }
                else
                {
                    artifactEntryService.save(e, false);
                }
            } 
            finally
            {
                lock.unlock();
            }
        }
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
            
            Runnable job = () -> downloadRemotePackageFeed(storage.getId(), repository.getId(), npmSearchRequest.getPackageId());
            if (packageCount.longValue() == 0)
            {
                //Syncronously fetch remote package feed if ve have no cached packages
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
