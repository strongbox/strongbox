package org.carlspring.strongbox.storage.repository;

/**
 * @author carlspring
 */
public interface RepositoryFactory
{

    RepositoryDto createRepository(String repositoryId);

}
