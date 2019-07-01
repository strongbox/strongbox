package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.repository.RepositoryData;

public interface StoragesConfigurationManager
{

    RepositoryData getRepository(String storageAndRepositoryId);

    RepositoryData getRepository(String storageId,
                             String repositoryId);

    StorageData getStorage(String storageId);

}
