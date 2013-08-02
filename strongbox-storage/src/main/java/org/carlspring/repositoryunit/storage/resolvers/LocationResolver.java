package org.carlspring.repositoryunit.storage.resolvers;

import org.apache.maven.artifact.Artifact;

import java.io.InputStream;

/**
 * @author mtodorov
 */
public interface LocationResolver
{

    InputStream getInputStreamForArtifact(String repository, Artifact artifact);

    void initialize();

}
