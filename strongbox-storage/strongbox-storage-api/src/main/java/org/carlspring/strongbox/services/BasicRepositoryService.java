package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public interface BasicRepositoryService extends ConfigurationService
{

    boolean containsArtifact(Repository repository, Artifact artifact);

    boolean containsPath(Repository repository, String path);

    String getPathToArtifact(Repository repository, Artifact artifact);

}
