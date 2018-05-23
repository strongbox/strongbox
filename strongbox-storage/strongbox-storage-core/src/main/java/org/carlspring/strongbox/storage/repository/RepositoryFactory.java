package org.carlspring.strongbox.storage.repository;

/**
 * @author carlspring
 */
public interface RepositoryFactory
{

    MutableRepository createRepository(String repositoryId);

}
