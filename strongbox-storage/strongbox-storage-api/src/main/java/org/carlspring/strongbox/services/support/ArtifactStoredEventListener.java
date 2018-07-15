package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactStoredEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactStoredEventListener.class);

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @AsyncEventListener
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED.getType())
        {
            return;
        }

        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive());

        RepositoryPath repositoryPath = event.getPath();
        if (repositoryPath instanceof TempRepositoryPath)
        {
            repositoryPath = ((TempRepositoryPath) repositoryPath).getTempTarget();
        }
        final Repository repository = repositoryPath.getRepository();
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final Set<String> archiveFilenames = layoutProvider.listArchiveFilenames(repositoryPath);
        if (!archiveFilenames.isEmpty())
        {
            ArtifactEntry artifactEntry;
            try
            {
                artifactEntry = repositoryPath.getArtifactEntry();
            }
            catch (IOException e)
            {
                logger.error(String.format("Unable to get artifact entry for path %s.", repositoryPath), e);
                return;
            }
            if (artifactEntry == null)
            {
                logger.warn(String.format("Unable to store archive filenames for path %s. ArtifactEntry is null.",
                                          repositoryPath));
                return;
            }
            artifactEntry = artifactEntryService.lockOne(artifactEntry.getObjectId());
            ArtifactArchiveListing artifactArchiveListing = artifactEntry.getArtifactArchiveListing();
            logger.debug(
                    String.format("XXXXXXXXXXXXXXXX Previous state of artifact entry [%s].", artifactEntry));
            if (artifactArchiveListing == null)
            {
                artifactArchiveListing = new ArtifactArchiveListing();
                artifactEntry.setArtifactArchiveListing(artifactArchiveListing);
            }
            artifactArchiveListing.setFilenames(archiveFilenames);
            logger.debug(
                    String.format("XXXXXXXXXXXXXXXX Current state of artifact entry [%s].", artifactEntry));
            artifactEntryService.save(artifactEntry, true);
        }

    }
}
