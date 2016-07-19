package org.carlspring.strongbox.storage.validation.version;

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
     */
    void validate(Repository repository, Artifact artifact) throws VersionValidationException;

}
