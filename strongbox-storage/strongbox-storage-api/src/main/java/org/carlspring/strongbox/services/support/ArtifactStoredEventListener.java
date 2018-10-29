package org.carlspring.strongbox.services.support;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.AsyncArtifactEntryHandler;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactStoredEventListener extends AsyncArtifactEntryHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactStoredEventListener.class);

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    private RepositoryPathLock repositoryPathLock;
    
    public ArtifactStoredEventListener()
    {
        super(ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED);
    }

    @Override
    protected ArtifactEntry handleEvent(RepositoryPath repositoryPath) throws IOException
    {
        ArtifactEntry artifactEntry = repositoryPath.getArtifactEntry();
        
        if (artifactEntry == null)
        {
            logger.warn(String.format("No [%s] for [%s].",
                                      ArtifactEntry.class.getSimpleName(),
                                      repositoryPath));

            return null;
        }
        
        final Repository repository = repositoryPath.getRepository();
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        
        ReadWriteLock lockSource = repositoryPathLock.lock(repositoryPath);
        Lock lock = lockSource.readLock();
        lock.lock();
        
        final Set<String> archiveFilenames;
        try
        {
            archiveFilenames = layoutProvider.listArchiveFilenames(repositoryPath);
        } 
        finally
        {
            lock.unlock();
        }
        
        if (archiveFilenames.isEmpty())
        {
            return artifactEntry;
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
