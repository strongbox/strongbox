package org.carlspring.strongbox.storage.validation.groupid;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.validation.LowercaseValidator;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

/**
 * Created by dinesh on 12/6/17.
 */
@Component("mavenGroupIdLowercaseValidator")
public class MavenGroupIdLowercaseValidator
        implements LowercaseValidator
{

    MavenArtifactCoordinates coordinates;


    @Override
    public void validateCase(RepositoryPath repositoryPath)
            throws LowercaseValidationException,
                   IOException
    {
        RepositoryFileAttributes repositoryFileAttributes = this.getAttributes(repositoryPath);

        coordinates = (MavenArtifactCoordinates) repositoryFileAttributes.getCoordinates();
        if (!coordinates.getGroupId().toLowerCase().equals(coordinates.getGroupId()))
        {
            throw new LowercaseValidationException("Group Id should be defined in lower case");
        }
    }

    public RepositoryFileAttributes getAttributes(RepositoryPath repositoryPath)
            throws IOException
    {
        return Files.readAttributes(repositoryPath, RepositoryFileAttributes.class);
    }


}

