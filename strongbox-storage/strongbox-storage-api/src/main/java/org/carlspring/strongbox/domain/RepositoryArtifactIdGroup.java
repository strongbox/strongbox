package org.carlspring.strongbox.domain;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class RepositoryArtifactIdGroup
        extends ArtifactGroup
{

    private String storageId;

    private String repositoryId;

    private String id;

    public static Map<String, ? extends Object> properties(String storageId,
                                                           String repositoryId,
                                                           String id)
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put("storageId", storageId);
        properties.put("repositoryId", repositoryId);
        properties.put("id", id);
        return properties;
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
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
