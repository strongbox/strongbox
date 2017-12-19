package org.carlspring.strongbox.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.event.CommonEventListener;
import org.carlspring.strongbox.providers.repository.RepositoryPageRequest;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.xml.configuration.repository.NugetRepositoryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.client.NugetClient;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.query.AndExpression;
import ru.aristar.jnuget.query.Expression;
import ru.aristar.jnuget.query.IdEqIgnoreCase;
import ru.aristar.jnuget.query.VersionEq;
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

    public void downloadRemoteFeed(String storageId,
                                   String repositoryId)
        throws RepositoryInitializationException,
        ArtifactTransportException
    {
        downloadRemoteFeed(storageId, repositoryId, null, null, null);
    }

    public void downloadRemoteFeed(String storageId,
                                   String repositoryId,
                                   Expression filter,
                                   String searchTerm,
                                   String targetFramework)
        throws RepositoryInitializationException,
        ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        Optional<NugetRepositoryConfiguration> repositoryConfiguration = Optional.ofNullable((NugetRepositoryConfiguration) repository.getRepositoryConfiguration());
        Integer remoteFeedPageSize = repositoryConfiguration.map(c -> c.getRemoteFeedPageSize())
                                                            .orElse(REMOTE_FEED_PAGE_SIZE);
        for (int i = 0; true; i++)
        {
            if (!downloadRemoteFeed(storageId, repositoryId, filter, searchTerm, targetFramework,
                                    i * remoteFeedPageSize, remoteFeedPageSize))
            {
                break;
            }
        }
    }

    public boolean downloadRemoteFeed(String storageId,
                                      String repositoryId,
                                      Expression filter,
                                      String searchTerm,
                                      String targetFramework,
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

        try (NugetClient nugetClient = new NugetClient())
        {
            nugetClient.setUrl(remoteRepositoryUrl);
            PackageFeed packageFeed;
            try
            {
                packageFeed = nugetClient.getPackages(filter == null ? null : filter.toString(), searchTerm,
                                                      top == -1 ? null : top,
                                                      targetFramework, skip);
            }
            catch (IOException | URISyntaxException e)
            {
                throw new ArtifactTransportException(e);
            }
            if (packageFeed == null || packageFeed.getEntries() == null || packageFeed.getEntries().size() == 0)
            {
                return false;
            }
            Set<NugetArtifactCoordinates> artifactToSaveSet = new HashSet<>();
            for (PackageEntry packageEntry : packageFeed.getEntries())
            {
                String packageId = packageEntry.getProperties().getId();
                packageId = packageId == null ? packageEntry.getTitle() : packageId;
                String packageVersion = packageEntry.getProperties().getVersion().toString();

                NugetArtifactCoordinates c = new NugetArtifactCoordinates(packageId, packageVersion, "nupkg");
                
                if (!artifactEntryService.artifactExists(storageId, repositoryId, c.toPath()))
                {
                    artifactToSaveSet.add(c);
                }
            }
            for (NugetArtifactCoordinates c : artifactToSaveSet)
            {
                RemoteArtifactEntry remoteArtifactEntry = new RemoteArtifactEntry();
                remoteArtifactEntry.setStorageId(storageId);
                remoteArtifactEntry.setRepositoryId(repositoryId);
                remoteArtifactEntry.setArtifactCoordinates(c);
                artifactEntryService.save(remoteArtifactEntry);
            }
            return true;
        }
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    @Component
    public class RepositorySearchEventListener implements CommonEventListener<RemoteRepositorySearchEvent>
    {

        @Override
        public void handle(RemoteRepositorySearchEvent event)
        {
            RepositorySearchRequest repositorySearchRequest = event.getSearchRequest();
            RepositoryPageRequest repositoryPageRequest = event.getPageRequest();

            Storage storage = getConfiguration().getStorage(repositorySearchRequest.getStorageId());
            Repository repository = storage.getRepository(repositorySearchRequest.getRepositoryId());
            RemoteRepository remoteRepository = repository.getRemoteRepository();
            if (remoteRepository == null)
            {
                return;
            }

            Map<String, String> coordinates = repositorySearchRequest.getCoordinates();
            String packageId = coordinates.get(NugetArtifactCoordinates.ID);
            String version = coordinates.get(NugetArtifactCoordinates.VERSION);

            Long packageCount = artifactEntryService.countArtifacts(storage.getId(), repository.getId(), coordinates,
                                                                    repositorySearchRequest.isStrict());
            logger.debug(String.format("Remote repository [%s] cached package count is [%s]", repository.getId(),
                                       packageCount));

            Expression filter = repositorySearchRequest.isStrict() ? createPackageEq(packageId, null) : null;
            filter = createVersionEq(version, filter);
            String searchTerm = !repositorySearchRequest.isStrict() ? packageId : null;

            try
            {
                int remotePackageCount;
                try (NugetClient nugetClient = new NugetClient())
                {
                    nugetClient.setUrl(remoteRepository.getUrl());
                    remotePackageCount = nugetClient.getPackageCount(filter == null ? null : filter.toString(),
                                                                     searchTerm,
                                                                     null);
                    logger.debug(String.format("Remote repository [%s] remote package count is [%s]",
                                               repository.getId(), packageCount));
                }

                if (Long.valueOf(remotePackageCount).compareTo(packageCount) == 0)
                {
                    logger.debug(String.format("No need to download remote feed, there was no changes in remote repository [%s] against local cache.",
                                               remoteRepository.getUrl()));
                    return;
                }

                logger.debug(String.format("Downloading remote feed for [%s].",
                                           remoteRepository.getUrl()));

                downloadRemoteFeed(repositorySearchRequest.getStorageId(), repositorySearchRequest.getRepositoryId(),
                                   filter,
                                   searchTerm, null, repositoryPageRequest.getSkip(),
                                   repositoryPageRequest.getLimit());
            }
            catch (Exception e)
            {
                logger.error(String.format("Failed to fetch Nuget remote feed [%s]",
                                           repositorySearchRequest.getCoordinates()),
                             e);
            }
        }

    }

    public static Expression createVersionEq(String version,
                                             Expression filter)
    {
        if (version == null || version.trim().isEmpty())
        {
            return filter;
        }
        VersionEq versionEq;
        try
        {
            versionEq = new VersionEq(Version.parse(version));
            filter = filter == null ? versionEq : new AndExpression(filter, versionEq);
        }
        catch (NugetFormatException e)
        {
            logger.error(String.format("Failed to parse Nuget version [%s]", version), e);
        }

        return filter;
    }

    public static Expression createPackageEq(String packageId,
                                             Expression filter)
    {
        if (packageId == null || packageId.trim().isEmpty())
        {
            return filter;
        }
        IdEqIgnoreCase idEqIgnoreCase = new IdEqIgnoreCase(packageId);
        filter = filter == null ? idEqIgnoreCase : new AndExpression(filter, idEqIgnoreCase);

        return filter;
    }
}
