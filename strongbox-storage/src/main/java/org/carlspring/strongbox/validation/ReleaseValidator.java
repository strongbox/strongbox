package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.Artifact;

/**
 * @author stodorov
 */
public class ReleaseValidator implements VersionValidator
{


    /**
     * Matches versions:
     *  1
     *  1.0
     *  1.0-SNAPSHOT
     */
    @Override
    public boolean validate(Repository repository,
                            Artifact artifact)
    {
        String version = artifact.getVersion();
        return version.matches("^([0-9]+)(\\.([0-9]+))?$");

    }

}
