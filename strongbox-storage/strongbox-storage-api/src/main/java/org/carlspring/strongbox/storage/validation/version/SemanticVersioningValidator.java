package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;

import org.semver.Version;
import org.springframework.stereotype.Component;

@Component
public class SemanticVersioningValidator implements VersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        // TODO: SB-1002: Split the version validators from the the deployment validators
        // return repository.getVersionValidators().contains(VersionValidatorType.SEM_VER);
        return false;
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
        throws VersionValidationException
    {
        String version = coordinates.getVersion();
        try
        {
            Version.parse(version);
        }
        catch (IllegalArgumentException e)
        {
            throw new VersionValidationException(String.format("Artifact version [%s] should follow the Semantic " +
                                                               "Versioning specification (https://semver.org/).",
                                                               version));
        }
    }

}
