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
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.springframework.stereotype.Component;

import ru.aristar.jnuget.client.NugetClient;
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

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Override
    public void downloadRemoteIndex(String storageId,
                                    String repositoryId)
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
            int packageCount;
            try
            {
                packageCount = nugetClient.getPackageCount(false);
            }
            catch (IOException | URISyntaxException e)
            {
                throw new ArtifactTransportException(e);
            }
            int pageCount = packageCount / 100 + (packageCount % 100 == 0 ? 0 : 1);
            for (int i = 0; i < pageCount; i++)
            {
                PackageFeed packageFeed;
                try
                {
                    packageFeed = nugetClient.getPackages(null, null, 100, null, i * 100);
                }
                catch (IOException | URISyntaxException e)
                {
                    throw new ArtifactTransportException(e);
                }

                Set<NugetHierarchicalArtifactCoordinates> artifactToSaveSet = new HashSet<>();
                for (PackageEntry packageEntry : packageFeed.getEntries())
                {
                    String packageId = packageEntry.getTitle();
                    String packageVersion = packageEntry.getProperties().getVersion().toString();

                    NugetHierarchicalArtifactCoordinates c = new NugetHierarchicalArtifactCoordinates(packageId,
                            packageVersion,
                            "nupkg");
                    if (!artifactEntryService.existsByUuid(c.toPath()))
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

}
