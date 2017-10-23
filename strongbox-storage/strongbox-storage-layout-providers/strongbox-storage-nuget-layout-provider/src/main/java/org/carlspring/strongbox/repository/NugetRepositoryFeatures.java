package org.carlspring.strongbox.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.event.CommonEventListener;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
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
        for (int i = 0; true; i++)
        {
            if (!downloadRemoteFeed(storageId, repositoryId, filter, searchTerm, targetFramework, 100, i * 100))
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
        throws ArtifactTransportException,
        RepositoryInitializationException
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
            Set<NugetHierarchicalArtifactCoordinates> artifactToSaveSet = new HashSet<>();
            for (PackageEntry packageEntry : packageFeed.getEntries())
            {
                String packageId = packageEntry.getTitle();
                String packageVersion = packageEntry.getProperties().getVersion().toString();

                NugetHierarchicalArtifactCoordinates c = new NugetHierarchicalArtifactCoordinates(packageId,
                        packageVersion,
                        "nupkg");
                if (!artifactEntryService.exists(storageId, repositoryId, c.toPath()))
                {
                    artifactToSaveSet.add(c);
                }
            }
            for (NugetHierarchicalArtifactCoordinates c : artifactToSaveSet)
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
            RepositorySearchRequest repositorySearchRequest = event.getEventData();
            Map<String, String> coordinates = repositorySearchRequest.getCoordinates();
            String packageId = coordinates.get(NugetArtifactCoordinates.ID);
            String version = coordinates.get(NugetArtifactCoordinates.VERSION);

            Expression filter = repositorySearchRequest.isStrict() ? createPackageEq(packageId, null) : null;
            filter = createVersionEq(version, filter);
            String searchTerm = !repositorySearchRequest.isStrict() ? packageId : null;

            try
            {
                downloadRemoteFeed(repositorySearchRequest.getStorageId(), repositorySearchRequest.getRepositoryId(),
                                   filter,
                                   searchTerm, null, repositorySearchRequest.getSkip(),
                                   repositorySearchRequest.getLimit());
            }
            catch (RepositoryInitializationException | ArtifactTransportException e)
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
