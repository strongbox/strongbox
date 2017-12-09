package org.carlspring.strongbox.storage.validation.groupId;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;

/**
 * Created by dinesh on 12/6/17.
 */
public interface GroupIdValidator {

    /**
     * This method checks if the groupId component of the path is in lowercase
     * @param repositoryPath
     * @throws GroupIdValidationException
     * @throws ProviderImplementationException
     * @throws IOException
     */
    void validateGroupIdCase(RepositoryPath repositoryPath)
            throws GroupIdValidationException,
            ProviderImplementationException,
            IOException;


}
