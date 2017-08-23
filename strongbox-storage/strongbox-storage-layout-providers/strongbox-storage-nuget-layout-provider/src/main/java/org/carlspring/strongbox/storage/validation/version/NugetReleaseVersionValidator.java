package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component("NugetReleaseVersionValidator")
public class NugetReleaseVersionValidator
        implements VersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        return repository.getVersionValidators().contains(VersionValidatorType.RELEASE);
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isRelease(version) && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository which does not accept release policy!");
        }
        if (!isRelease(version) && repository.acceptsReleases() && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a snapshot artifact to a repository with a release policy!");
        }
    }

    public boolean isRelease(String version)
    {
        return StringUtils.isNotBlank(version) && !StringUtils.endsWithIgnoreCase(version, "-SNAPSHOT");
    }

}
