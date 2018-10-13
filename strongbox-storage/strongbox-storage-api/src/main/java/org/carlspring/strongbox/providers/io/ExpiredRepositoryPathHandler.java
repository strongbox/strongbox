package org.carlspring.strongbox.providers.io;

/**
 * @author Przemyslaw Fusik
 */
public interface ExpiredRepositoryPathHandler
{

    default boolean supports(RepositoryPath repositoryPath)
    {
        return true;
    }

    void handleExpiration(RepositoryPath repositoryPath);

}
