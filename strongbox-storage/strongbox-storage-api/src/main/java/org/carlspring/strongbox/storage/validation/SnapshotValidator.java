package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.Artifact;

/**
 * @author stodorov
 */
public class SnapshotValidator implements VersionValidator
{

    /**
     * Matches versions:
     * 1.0-20131004
     * 1.0-20131004.115330
     * 1.0-20131004.115330-1
     */
    @Override
    public boolean validate(Repository repository, Artifact artifact)
    {
        String version = artifact.getVersion();
        return version != null &&
               version.matches("^([0-9]+)(\\.([0-9]+))(-(SNAPSHOT|([0-9]+)(\\.([0-9]+)(-([0-9]+))?)?))$");
    }

}
