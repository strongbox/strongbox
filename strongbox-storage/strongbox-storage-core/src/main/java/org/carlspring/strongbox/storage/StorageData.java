package org.carlspring.strongbox.storage;

import java.util.Map;

import org.carlspring.strongbox.storage.repository.RepositoryData;

public interface StorageData
{

    RepositoryData getRepository(String repositoryId);

    String getId();

    String getBasedir();

    Map<String, ? extends RepositoryData> getRepositories();

    boolean containsRepository(String repositoryId);

}