package org.carlspring.strongbox.event;

/**
 * @author carlspring
 */
public class RepositoryBasedEvent extends Event
{

    private String storageId;

    private String repositoryId;

    private String path;


    public RepositoryBasedEvent()
    {
    }

    public RepositoryBasedEvent(String storageId,
                                String repositoryId,
                                int type)
    {
        setStorageId(storageId);
        setRepositoryId(repositoryId);
        setType(type);
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

}
