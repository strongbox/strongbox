package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.Artifact;
import org.springframework.stereotype.Component;

/**
 * @author stodorov
 */
@Component("snapshotVersionValidator")
public class SnapshotVersionValidator
        implements VersionValidator
{


    /**
     * Matches versions:
     * 1.0-20131004
     * 1.0-20131004.115330
     * 1.0-20131004.115330-1
     * 1.0.8-20151025.032208-1
     * 1.0.8-alpha-1-20151025.032208-1
     */
    @Override
    public void validate(Repository repository, Artifact artifact)
            throws VersionValidationException
    {
        String version = artifact.getVersion();
        if (isSnapshot(version) && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a SNAPSHOT artifact to a repository with a release policy!");
        }
        if (!isSnapshot(version) && repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository with a SNAPSHOT policy!");
        }
    }

    public boolean isSnapshot(String version)
    {
        return version != null && ArtifactUtils.isSnapshot(version);
    }

}
