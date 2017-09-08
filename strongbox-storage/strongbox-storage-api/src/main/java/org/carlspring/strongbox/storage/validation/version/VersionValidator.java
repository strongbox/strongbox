package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface VersionValidator
{

    default boolean supports(Repository repository)
    {
        return true;
    }

    /**
     * Checks if an artifact version is acceptable by the repository.
     *
     * @param repository  The repository.
     * @param coordinates The artifact being deployed.
     */
    void validate(Repository repository,
                  ArtifactCoordinates coordinates)
            throws VersionValidationException,
                   ProviderImplementationException,
                   IOException;

}
