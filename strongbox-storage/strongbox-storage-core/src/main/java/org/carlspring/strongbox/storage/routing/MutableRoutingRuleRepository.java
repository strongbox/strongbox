package org.carlspring.strongbox.storage.routing;

import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;
import static org.carlspring.strongbox.db.schema.Properties.STORAGE_ID;

import java.io.Serializable;

import org.carlspring.strongbox.configuration.ConfigurationUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public class MutableRoutingRuleRepository
        implements Serializable
{

    private String storageId;

    private String repositoryId;

    public MutableRoutingRuleRepository()
    {
    }

    public MutableRoutingRuleRepository(String storageAndRepositoryId)
    {
        this(ConfigurationUtils.getStorageId(null, storageAndRepositoryId),
             ConfigurationUtils.getRepositoryId(storageAndRepositoryId));
    }

    
    @JsonCreator
    public MutableRoutingRuleRepository(@JsonProperty(STORAGE_ID) String storageId,
                                        @JsonProperty(REPOSITORY_ID) String repositoryId)
    {
        this.storageId = storageId;
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

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
}
