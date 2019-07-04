package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

public interface StoragesConfigurationManager
{

    Repository getRepository(String storageAndRepositoryId);

    Repository getRepository(String storageId,
                             String repositoryId);

    Storage getStorage(String storageId);

}
