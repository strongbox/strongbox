package org.carlspring.strongbox.forms.storage.routing;

/**
 * @author Przemyslaw Fusik
 */
public class RoutingRuleRepositoryForm
{
    private String repositoryId;

    private String storageId;

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
}
