package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ArtifactEventListenerRegistry
        extends AbstractEventListenerRegistry<ArtifactEvent>
{


    public void dispatchArtifactUploadingEvent(String storageId, String repositoryId, String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADING.getType());

        dispatchEvent(event);
    }

    public void dispatchArtifactUploadedEvent(String storageId, String repositoryId, String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED.getType());

        dispatchEvent(event);
    }

    public void dispatchArtifactChecksumUploadedEvent(String storageId, String repositoryId, String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_CHECKSUM_UPLOADED.getType());

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadingEvent(String storageId, String repositoryId, String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADING.getType());

        dispatchEvent(event);
    }

    public void dispatchArtifactDownloadedEvent(String storageId, String repositoryId, String path)
    {
        ArtifactEvent event = new ArtifactEvent(storageId,
                                                repositoryId,
                                                path,
                                                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED.getType());

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

        dispatchEvent(event);
    }

}
