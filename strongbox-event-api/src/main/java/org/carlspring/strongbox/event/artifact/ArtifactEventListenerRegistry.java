package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;

import javax.annotation.PostConstruct;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ArtifactEventListenerRegistry
        extends AbstractEventListenerRegistry<ArtifactEvent>
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEventListenerRegistry.class);

    @Autowired(required = false)
    private List<ArtifactEventListener> artifactEventListeners;

    @PostConstruct
    public void init()
    {
        if (artifactEventListeners != null)
        {
            artifactEventListeners.forEach(this::addListener);
        }
    }

    public void dispatchArtifactUploadingEvent(String storageId,
                                               String repositoryId,
                                               String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADING event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactUploadedEvent(String storageId,
                                              String repositoryId,
                                              String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataFileUploadedEvent(String storageId,
                                                          String repositoryId,
                                                          String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactFileUpdatedEvent(String storageId,
                                                 String repositoryId,
                                                 String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataFileUpdatedEvent(String storageId,
                                                         String repositoryId,
                                                         String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPDATED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumUploadedEvent(String storageId,
                                                      String repositoryId,
                                                      String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumFileUpdatedEvent(String storageId,
                                                         String repositoryId,
                                                         String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPDATED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadingEvent(String storageId,
                                                 String repositoryId,
                                                 String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADING event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadedEvent(String storageId,
                                                String repositoryId,
                                                String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactCopyingEvent(String srcStorageId,
                                             String srcRepositoryId,
                                             String destStorageId,
                                             String destRepositoryId,
                                             String path)
    {
        ArtifactEvent event = new ArtifactEvent(srcStorageId,
                                                srcRepositoryId,
                                                destStorageId,
                                                destRepositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPYING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPYING event for " +
                     srcStorageId + ":" + srcRepositoryId + "/" + path + " to " + destStorageId + ":" +
                     destRepositoryId + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactCopiedEvent(String srcStorageId,
                                            String srcRepositoryId,
                                            String destStorageId,
                                            String destRepositoryId,
                                            String path)
    {
        ArtifactEvent event = new ArtifactEvent(srcStorageId,
                                                srcRepositoryId,
                                                destStorageId,
                                                destRepositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPIED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPIED event for " +
                     srcStorageId + ":" + srcRepositoryId + "/" + path + " to " + destStorageId + ":" +
                     destRepositoryId + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMovingEvent(String srcStorageId,
                                            String srcRepositoryId,
                                            String destStorageId,
                                            String destRepositoryId,
                                            String path)
    {
        ArtifactEvent event = new ArtifactEvent(srcStorageId,
                                                srcRepositoryId,
                                                destStorageId,
                                                destRepositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVING event for " +
                     srcStorageId + ":" + srcRepositoryId + "/" + path + " to " + destStorageId + ":" +
                     destRepositoryId + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMovedEvent(String srcStorageId,
                                           String srcRepositoryId,
                                           String destStorageId,
                                           String destRepositoryId,
                                           String path)
    {
        ArtifactEvent event = new ArtifactEvent(srcStorageId,
                                                srcRepositoryId,
                                                destStorageId,
                                                destRepositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED event for " +
                     srcStorageId + ":" + srcRepositoryId + "/" + path + " to " + destStorageId + ":" +
                     destRepositoryId + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactPathDeletedEvent(String storageId,
                                                 String repositoryId,
                                                 String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactArchivingEvent(String storageId,
                                               String repositoryId,
                                               String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_ARCHIVING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactArchivedEvent(String storageId,
                                              String repositoryId,
                                              String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_ARCHIVED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataDownloadedEvent(String storageId,
                                                        String repositoryId,
                                                        String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataDownloadingEvent(String storageId,
                                                         String repositoryId,
                                                         String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADING event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumDownloadedEvent(String storageId,
                                                        String repositoryId,
                                                        String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADED event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumDownloadingEvent(String storageId,
                                                         String repositoryId,
                                                         String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADING event for " +
                     storageId + ":" + repositoryId + "/" + path + "...");

        dispatchEvent(event);
    }

}
