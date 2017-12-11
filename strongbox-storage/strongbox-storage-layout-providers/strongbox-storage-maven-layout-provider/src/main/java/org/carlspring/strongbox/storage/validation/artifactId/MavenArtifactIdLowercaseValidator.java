package org.carlspring.strongbox.storage.validation.artifactId;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.validation.exceptions.LowercaseValidationException;
import org.carlspring.strongbox.storage.validation.groupId.LowercaseValidator;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

/**
 * Created by dinesh on 12/10/17.
 */
@Component("mavenArtifactIdLowercaseValidator")
public class MavenArtifactIdLowercaseValidator
        implements LowercaseValidator
{

    MavenArtifactCoordinates coordinates;

    @Override
    public void validateCase(RepositoryPath repositoryPath)
            throws LowercaseValidationException, ProviderImplementationException, IOException
    {
        RepositoryFileAttributes repositoryFileAttributes = this.getAttributes(repositoryPath);

        coordinates = (MavenArtifactCoordinates) repositoryFileAttributes.getCoordinates();
        if (!coordinates.getArtifactId().toLowerCase().equals(coordinates.getArtifactId()))
        {
            throw new LowercaseValidationException("Artifact Id should be defined in lower case");
        }
    }

    public RepositoryFileAttributes getAttributes(RepositoryPath repositoryPath)
            throws IOException
    {
        return Files.readAttributes(repositoryPath, RepositoryFileAttributes.class);
    }
}
