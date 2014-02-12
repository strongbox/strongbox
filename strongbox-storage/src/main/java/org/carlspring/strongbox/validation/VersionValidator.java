package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public interface VersionValidator
{

    /**
     * Checks if an artifact version is acceptable by the repository.
     *
     * @param repository    The repository.
     * @param artifact      The artifact being deployed.
     * @return              True, if the repository accepts this kind of version;
     *                      false otherwise.
     */
    boolean validate(Repository repository, Artifact artifact);

}
