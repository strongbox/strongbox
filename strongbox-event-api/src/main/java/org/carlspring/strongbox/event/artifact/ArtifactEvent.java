package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.RepositoryBasedEvent;

/**
 * @author mtodorov
 */
public class ArtifactEvent extends RepositoryBasedEvent
{

    /**
     * The destination storage ID.
     *
     * Note: This should only be used for events that involve
     * a source and destination repository such as copying, moving, etc.
     */
    private String destinationStorageId;

    /**
     * The destination repository ID.
     *
     * Note: This should only be used for events that involve
     * a source and destination repository such as copying, moving, etc.
     */
    private String destinationRepositoryId;



    public ArtifactEvent(String storageId,
                         String repositoryId,
                         String path,
                         int type)
    {
        super(storageId, repositoryId, type);
        setPath(path);
    }

    public ArtifactEvent(String srcStorageId,
                         String srcRepositoryId,
                         String destStorageId,
                         String destRepositoryId,
                         String path,
                         int type)
    {
        super(srcStorageId, srcRepositoryId, type);
        setDestinationStorageId(destStorageId);
        setDestinationRepositoryId(destRepositoryId);
        setPath(path);
    }

    public String getDestinationStorageId()
    {
        return destinationStorageId;
    }

    public void setDestinationStorageId(String destinationStorageId)
    {
        this.destinationStorageId = destinationStorageId;
    }

    public String getDestinationRepositoryId()
    {
        return destinationRepositoryId;
    }

    public void setDestinationRepositoryId(String destinationRepositoryId)
    {
        this.destinationRepositoryId = destinationRepositoryId;
    }

}
