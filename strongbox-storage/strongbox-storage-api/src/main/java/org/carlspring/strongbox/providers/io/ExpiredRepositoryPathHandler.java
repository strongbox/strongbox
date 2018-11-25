package org.carlspring.strongbox.providers.io;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public interface ExpiredRepositoryPathHandler
{

    default boolean supports(RepositoryPath repositoryPath)
    {
        return true;
    }

    void handleExpiration(RepositoryPath repositoryPath)
            throws IOException;

}
