package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

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

    default boolean supports(String layoutProvider)
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
                   ArtifactCoordinatesValidationException,
                   IOException;

    /**
     * Returns the list of supported validators. By default, it returns an empty set, meaning that there is
     * no explicit list of supported providers to be limited to, (hence it accepts any provider).
     *
     * @return
     */
    default Set<String> getSupportedLayoutProviders()
    {
        return Collections.emptySet();
    }

}
