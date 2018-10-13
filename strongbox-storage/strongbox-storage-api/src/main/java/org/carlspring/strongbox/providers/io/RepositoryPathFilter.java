package org.carlspring.strongbox.providers.io;

/**
 * @author Przemyslaw Fusik
 */
@FunctionalInterface
public interface RepositoryPathFilter
{

    boolean matches(RepositoryPath repositoryPath);
}
