package org.carlspring.strongbox.storage;

import java.util.Map;

import org.carlspring.strongbox.storage.repository.Repository;

public interface Storage
{

    Repository getRepository(String repositoryId);

    String getId();

    String getBasedir();

    Map<String, ? extends Repository> getRepositories();

    boolean containsRepository(String repositoryId);

}