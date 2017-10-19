package org.carlspring.strongbox.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.event.CommonEventListener;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;
import org.carlspring.strongbox.providers.repository.event.RepositorySearchEvent;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.aristar.jnuget.client.NugetClient;
import ru.aristar.jnuget.query.Expression;
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
        throws ArtifactTransportException,
        RepositoryInitializationException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository == null)
        {
            return;
        }
        String remoteRepositoryUrl = remoteRepository.getUrl();

        try (NugetClient nugetClient = new NugetClient())
        {
            nugetClient.setUrl(remoteRepositoryUrl);
            for (int i = 0; true; i++)
            {
                PackageFeed packageFeed;
                try
                {
                    packageFeed = nugetClient.getPackages(filter == null ? null : filter.toString(), searchTerm, 100,
                                                          targetFramework, i * 100);
                }
                catch (IOException | URISyntaxException e)
                {
                    throw new ArtifactTransportException(e);
                }
                if (packageFeed.getEntries().isEmpty())
                {
                    break;
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

            }
        }
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    @Component
    public class RepositorySearchEventListener implements CommonEventListener<RepositorySearchEvent> {

        @Override
        public void handle(RepositorySearchEvent event)
        {
            RepositorySearchRequest repositorySearchRequest = event.getEventData();
            //TODO: construct filter by coordinates
            Expression filter = null;
            String searchTerm = null;
            try
            {
                downloadRemoteFeed(repositorySearchRequest.getStorageId(), repositorySearchRequest.getStorageId(), filter,
                                   searchTerm, null);
            }
            catch (RepositoryInitializationException | ArtifactTransportException e)
            {
                logger.error(String.format("Failed to fetch Nuget remote feed",
                                           repositorySearchRequest.getCoordinates()),
                             e);
            }
        }
        
    }
    
}
