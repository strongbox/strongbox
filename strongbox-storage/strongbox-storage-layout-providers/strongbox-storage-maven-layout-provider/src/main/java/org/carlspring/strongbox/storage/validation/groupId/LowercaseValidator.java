package org.carlspring.strongbox.storage.validation.groupId;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.validation.exceptions.LowercaseValidationException;

import java.io.IOException;

/**
 * Created by dinesh on 12/6/17.
 */
public interface LowercaseValidator
{

    /**
     * This method checks if the groupId component of the path is in lowercase
     *
     * @param repositoryPath
     * @throws LowercaseValidationException
     * @throws ProviderImplementationException
     * @throws IOException
     */
    void validateCase(RepositoryPath repositoryPath)
            throws LowercaseValidationException,
                   ProviderImplementationException,
                   IOException;


}
