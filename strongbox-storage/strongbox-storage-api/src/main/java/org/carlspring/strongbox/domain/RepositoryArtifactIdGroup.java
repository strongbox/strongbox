package org.carlspring.strongbox.domain;

import javax.persistence.Entity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class RepositoryArtifactIdGroup
        extends ArtifactGroup
{

    private String storageId;
    private String repositoryId;

    public RepositoryArtifactIdGroup()
    {
    }

    public RepositoryArtifactIdGroup(String storageId,
                                     String repositoryId,
                                     String id)
    {
        super(id);
        this.storageId = storageId;
        this.repositoryId = repositoryId;
    }
    
    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getId()
    {
        return getName();
    }

}
