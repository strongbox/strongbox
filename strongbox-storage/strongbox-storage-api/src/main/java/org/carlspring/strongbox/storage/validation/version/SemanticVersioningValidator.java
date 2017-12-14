package org.carlspring.strongbox.storage.validation.version;

import java.io.IOException;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.semver.Version;
import org.springframework.stereotype.Component;

@Component
public class SemanticVersioningValidator implements VersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        return repository.getSemanticVersioning();
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
        throws VersionValidationException,
        ProviderImplementationException,
        IOException
    {
        String version = coordinates.getVersion();
        try
        {
            Version.parse(version);
        }
        catch (IllegalArgumentException e)
        {
            throw new VersionValidationException(
                    String.format("Artifact version [%s] should follow the Semantic Versioning specification (https://semver.org/).",
                                  version));
        }
    }

}
