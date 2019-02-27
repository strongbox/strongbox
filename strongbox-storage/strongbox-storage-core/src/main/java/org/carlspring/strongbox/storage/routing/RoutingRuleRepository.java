package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RoutingRuleRepository
        implements RepositoryIdentifiable
{

    private final String storageId;

    private final String repositoryId;

    public RoutingRuleRepository(MutableRoutingRuleRepository source)
    {
        this.storageId = source.getStorageId();
        this.repositoryId = source.getRepositoryId();
    }

    public String getStorageId()
    {
        return storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }
}
