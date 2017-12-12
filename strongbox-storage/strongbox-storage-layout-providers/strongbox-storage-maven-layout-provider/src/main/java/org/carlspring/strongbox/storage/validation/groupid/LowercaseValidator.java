package org.carlspring.strongbox.storage.validation.groupid;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;

/**
 * Created by dinesh on 12/6/17.
 */
public interface LowercaseValidator
{

    /**
     * This method checks if the respective part of the path is lowercase
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

