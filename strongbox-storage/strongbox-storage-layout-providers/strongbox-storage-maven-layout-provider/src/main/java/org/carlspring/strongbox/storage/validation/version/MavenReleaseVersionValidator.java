package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;

import org.springframework.stereotype.Component;

/**
 * @author stodorov
 */
@Component("MavenReleaseVersionValidator")
public class MavenReleaseVersionValidator
        implements VersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        return repository.getVersionValidators().contains(VersionValidatorType.RELEASE);
    }

    /**
     * Matches versions:
     * 1
     * 1.0
     * 1.0-SNAPSHOT
     */
    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isRelease(version) && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository with a SNAPSHOT policy!");
        }
        if (!isRelease(version) && repository.acceptsReleases() && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a SNAPSHOT artifact to a repository with a release policy!");
        }
    }

    public boolean isRelease(String version)
    {
        return version != null && ArtifactUtils.isReleaseVersion(version);
    }

}
