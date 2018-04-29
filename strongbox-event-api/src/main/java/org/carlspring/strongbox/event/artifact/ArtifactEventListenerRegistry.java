package org.carlspring.strongbox.event.artifact;

import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ArtifactEventListenerRegistry
        extends AbstractEventListenerRegistry
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

    public void dispatchArtifactUploadingEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADING event for " + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactUploadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED event for " + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataFileUploadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPLOADED event for " + path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactFileUpdatedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataFileUpdatedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPDATED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumUploadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPLOADED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumFileUpdatedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPDATED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPDATED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadingEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADING event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactCopyingEvent(Path srcPath,
                                             Path dstPath)
    {
        ArtifactEvent event = new ArtifactEvent(srcPath,
                                                dstPath,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPYING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPYING event for " +
                     srcPath + " to " + dstPath +
                     "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactCopiedEvent(Path srcPath,
                                            Path dstPath)
    {
        ArtifactEvent event = new ArtifactEvent(srcPath,
                                                dstPath,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPIED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_COPIED event for " +
                     srcPath + " to " + dstPath +
                     "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMovingEvent(Path srcPath,
                                            Path dstPath)
    {
        ArtifactEvent event = new ArtifactEvent(srcPath,
                                                dstPath,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVING event for " +
                     srcPath + " to " + dstPath +
                     "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMovedEvent(Path srcPath,
                                           Path dstPath)
    {
        ArtifactEvent event = new ArtifactEvent(srcPath,
                                                dstPath,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED event for " +
                     srcPath + " to " + dstPath +
                     "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactPathDeletedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactArchivingEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_ARCHIVING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_MOVED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactArchivedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_ARCHIVED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_ARCHIVED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataDownloadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactMetadataDownloadingEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_DOWNLOADING event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumDownloadedEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADED event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumDownloadingEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADING.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_DOWNLOADING event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactFetchedFromRemoteEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_FETCHED_FROM_REMOTE.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_FETCHED_FROM_REMOTE event for " +
                      path + "...");

        dispatchEvent(event);
    }

    public void dispatchArtifactStoredEvent(Path path)
    {
        ArtifactEvent event = new ArtifactEvent(path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED.getType());

        logger.debug("Dispatching ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED event for " +
                      path + "...");

        dispatchEvent(event);
    }

}
