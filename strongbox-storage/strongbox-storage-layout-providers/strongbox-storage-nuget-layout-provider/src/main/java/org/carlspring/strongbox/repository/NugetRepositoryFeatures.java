package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.nuget.NugetSearchRequest;
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
import org.carlspring.strongbox.xml.configuration.repository.NugetRepositoryConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.aristar.jnuget.rss.PackageEntry;
import ru.aristar.jnuget.rss.PackageFeed;

/**
 * 
 * @author carlspring
 * @author Sergey Bespalov
 */
@Component
public class NugetRepositoryFeatures
        implements RepositoryFeatures
{

    private static final int REMOTE_FEED_PAGE_SIZE = 1000;

    private static final Logger logger = LoggerFactory.getLogger(NugetRepositoryFeatures.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private RepositoryPathLock repositoryPathLock;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RedeploymentValidator redeploymentValidator;

    @Inject
    private GenericReleaseVersionValidator genericReleaseVersionValidator;

    @Inject
    private GenericSnapshotVersionValidator genericSnapshotVersionValidator;

    private Set<String> defaultMavenArtifactCoordinateValidators;

    @PostConstruct
    public void init()
    {
        defaultMavenArtifactCoordinateValidators = new LinkedHashSet<>(Arrays.asList(redeploymentValidator.getAlias(),
                                                                                     genericReleaseVersionValidator.getAlias(),
                                                                                     genericSnapshotVersionValidator.getAlias()));
    }

    public void downloadRemoteFeed(String storageId,
                                   String repositoryId)
            throws ArtifactTransportException
    {
        downloadRemoteFeed(storageId, repositoryId, new NugetSearchRequest());
    }

    public void downloadRemoteFeed(String storageId,
                                   String repositoryId,
                                   NugetSearchRequest nugetSearchRequest)
            throws ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        Optional<NugetRepositoryConfiguration> repositoryConfiguration = Optional.ofNullable((NugetRepositoryConfiguration) repository.getRepositoryConfiguration());
        Integer remoteFeedPageSize = repositoryConfiguration.map(c -> c.getRemoteFeedPageSize())
                                                            .orElse(REMOTE_FEED_PAGE_SIZE);
        for (int i = 0; true; i++)
        {
            if (!downloadRemoteFeed(storageId, repositoryId, nugetSearchRequest, i * remoteFeedPageSize,
                                    remoteFeedPageSize))
            {
                break;
            }
        }
    }

    public boolean downloadRemoteFeed(String storageId,
                                      String repositoryId,
                                      NugetSearchRequest nugetSearchRequest,
                                      int skip,
                                      int top)
        throws ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository == null)
        {
            return false;
        }
        String remoteRepositoryUrl = remoteRepository.getUrl();

        Paginator paginator = new Paginator();
        paginator.setLimit(top);
        paginator.setSkip(skip);

        PackageFeed packageFeed;
        Client restClient = proxyRepositoryConnectionPoolConfigurationService.getRestClient();
        try
        {
            logger.debug(String.format("Downloading remote feed for [%s].", remoteRepositoryUrl));

            WebTarget service = restClient.target(remoteRepository.getUrl());
            packageFeed = queryParams(service.path("Search()"), nugetSearchRequest, paginator).request()
                                                                                              .buildGet()
                                                                                              .invoke(PackageFeed.class);
            
            logger.debug(String.format("Downloaded remote feed for [%s], size [%s].",
                                       remoteRepository.getUrl(),
                                       Optional.of(packageFeed).map(f -> f.getEntries().size()).orElse(0)));

        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to fetch Nuget remote feed [%s]", remoteRepositoryUrl), e);
            return false;
        } finally
        {
            restClient.close();
        }

        if (packageFeed == null || packageFeed.getEntries() == null || packageFeed.getEntries().size() == 0)
        {
            return false;
        }

        parseFeed(repository, packageFeed);

        return true;
    }

    private void parseFeed(Repository repository,
                           PackageFeed packageFeed)
    {
        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        Set<ArtifactEntry> artifactToSaveSet = new HashSet<>();
        for (PackageEntry packageEntry : packageFeed.getEntries())
        {
            String packageId = packageEntry.getProperties().getId();
            packageId = packageId == null ? packageEntry.getTitle() : packageId;
            String packageVersion = packageEntry.getProperties().getVersion().toString();

            NugetArtifactCoordinates c = new NugetArtifactCoordinates(packageId, packageVersion, "nupkg");
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

            remoteArtifactEntry.setSizeInBytes(packageEntry.getProperties().getPackageSize());

            if (Boolean.TRUE.equals(packageEntry.getProperties().getIsLatestVersion()))
            {
                remoteArtifactEntry.getTagSet().add(lastVersionTag);
            }

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        for (ArtifactEntry e : artifactToSaveSet)
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, (NugetArtifactCoordinates) e.getArtifactCoordinates());

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

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    @Component
    @Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public class RepositorySearchEventListener
    {

        private NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();

        public NugetSearchRequest getNugetSearchRequest()
        {
            return nugetSearchRequest;
        }

        public void setNugetSearchRequest(NugetSearchRequest nugetSearchRequest)
        {
            this.nugetSearchRequest = nugetSearchRequest;
        }

        @EventListener
        public void handle(RemoteRepositorySearchEvent event)
        {
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

            logger.debug(String.format("Remote repository [%s] cached package count is [%s]", repository.getId(),
                                       packageCount));

            Client restClient = proxyRepositoryConnectionPoolConfigurationService.getRestClient();
            PackageFeed feed;
            try
            {
                WebTarget service = restClient.target(remoteRepository.getUrl());

                Long remotePackageCount = Long.valueOf(queryParams(service.path("Search()/$count"),
                                                                   nugetSearchRequest, new Paginator()).request()
                                                                                                       .buildGet()
                                                                                                       .invoke(String.class));
                logger.debug(String.format("Remote repository [%s] remote package count is [%s]",
                                           repository.getId(), remotePackageCount));

                if (Long.valueOf(remotePackageCount).compareTo(packageCount) == 0)
                {
                    logger.debug(String.format("No need to download remote feed, there was no changes in remote repository [%s] against local cache.",
                                               remoteRepository.getUrl()));
                    return;
                }

                logger.debug(String.format("Downloading remote feed for [%s].",
                                           remoteRepository.getUrl()));

                feed = queryParams(service.path("Search()"), nugetSearchRequest, event.getPaginator()).request()
                                                                                                      .buildGet()
                                                                                                      .invoke(PackageFeed.class);

                logger.debug(String.format("Downloaded remote feed for [%s], size [%s].",
                                           remoteRepository.getUrl(),
                                           Optional.of(feed).map(f -> f.getEntries().size()).orElse(0)));

            }
            catch (Exception e)
            {
                logger.error(String.format("Failed to fetch Nuget remote feed [%s]", remoteRepository.getUrl()), e);
                return;
            } 
            finally
            {
                restClient.close();
            }

            parseFeed(repository, feed);
        }

    }

    private WebTarget queryParams(WebTarget path,
                                  NugetSearchRequest nugetSearchRequest,
                                  Paginator paginator)
    {
        if (nugetSearchRequest.getFilter() != null && !nugetSearchRequest.getFilter().trim().isEmpty())
        {
            path = path.queryParam("$filter", nugetSearchRequest.getFilter().trim());
        }
        if (nugetSearchRequest.getSearchTerm() != null && !nugetSearchRequest.getSearchTerm().trim().isEmpty())
        {
            path = path.queryParam("searchTerm", nugetSearchRequest.getSearchTerm().trim());
        }
        if (nugetSearchRequest.getTargetFramework() != null
                && !nugetSearchRequest.getTargetFramework().trim().isEmpty())
        {
            path = path.queryParam("targetFramework", nugetSearchRequest.getTargetFramework().trim());
        }

        if (paginator.getSkip() != null && paginator.getSkip() > 0)
        {
            path = path.queryParam("$skip", paginator.getSkip());
        }
        if (paginator.getLimit() != null && paginator.getLimit() > 0)
        {
            path = path.queryParam("$top", paginator.getLimit());
        }

        return path;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return defaultMavenArtifactCoordinateValidators;
    }

}
