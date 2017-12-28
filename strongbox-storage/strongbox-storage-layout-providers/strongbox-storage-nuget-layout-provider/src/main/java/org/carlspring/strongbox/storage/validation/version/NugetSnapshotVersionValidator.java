package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
//TODO: invalid implementation
//@Component("NugetSnapshotVersionValidator")
public class NugetSnapshotVersionValidator
        implements NugetVersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        return NugetVersionValidator.super.supports(repository) &&
               repository.getVersionValidators().contains(VersionValidatorType.SNAPSHOT);
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isSnapshot(version) && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a SNAPSHOT artifact to a repository which doesn't accept SNAPSHOT policy!");
        }
        if (!isSnapshot(version) && repository.acceptsSnapshots() && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository with a SNAPSHOT policy!");
        }
    }

    public boolean isSnapshot(String version)
    {
        return StringUtils.isNotBlank(version) && StringUtils.endsWithIgnoreCase(version, "-SNAPSHOT");
    }

}

