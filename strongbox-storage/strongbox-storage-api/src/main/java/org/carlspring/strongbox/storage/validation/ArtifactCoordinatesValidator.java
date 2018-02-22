package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface ArtifactCoordinatesValidator
{

    void register();

    String getAlias();

    String getDescription();

    default boolean supports(Repository repository)
    {
        return true;
    }

    default boolean activeByDefault(Repository repository)
    {
        return false;
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
                   ArtifactCoordinatesValidationException,
                   IOException;

}
