package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface BasicRepositoryService
{

    boolean containsArtifact(Repository repository, Artifact artifact);

    boolean containsPath(Repository repository, String path);

    String getPathToArtifact(Repository repository, Artifact artifact);

}
