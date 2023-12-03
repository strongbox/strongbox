package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.artifact.AsyncArtifactEntryHandler;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactStoredEventListener
        extends AsyncArtifactEntryHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactStoredEventListener.class);

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    public ArtifactStoredEventListener()
    {
        super(ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED);
    }

    @Override
    protected ArtifactEntry handleEvent(RepositoryPath repositoryPath)
            throws IOException
    {
        ArtifactEntry artifactEntry = repositoryPath.getArtifactEntry();

        if (artifactEntry == null)
        {
            logger.warn("No [{}] for [{}].",
                        ArtifactEntry.class.getSimpleName(),
                        repositoryPath);

            return null;
        }

        final Repository repository = repositoryPath.getRepository();
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final Set<String> archiveFilenames = layoutProvider.listArchiveFilenames(repositoryPath);
        if (archiveFilenames.isEmpty())
        {
            return null;
        }

        ArtifactArchiveListing artifactArchiveListing = artifactEntry.getArtifactArchiveListing();
        if (artifactArchiveListing == null)
        {
            artifactArchiveListing = new ArtifactArchiveListing();
            artifactEntry.setArtifactArchiveListing(artifactArchiveListing);
        }
        artifactArchiveListing.setFilenames(archiveFilenames);

        return artifactEntry;
    }

}
