package org.carlspring.strongbox.util;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;

/**
 * @author mtodorov
 */
public class RepositoryUtils
{


    public static void checkRepositoryExists(String repositoryId, Repository repository)
            throws ArtifactResolutionException
    {
        if (repository == null)
        {
            throw new ArtifactResolutionException("Repository " + repositoryId + " does not exist.");
        }
    }

}
