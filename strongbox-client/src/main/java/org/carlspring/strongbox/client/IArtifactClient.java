package org.carlspring.strongbox.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * Artifact processing API.
 *
 * @author Alex Oreshkevich
 */
public interface IArtifactClient    // named with I prefix because of existing ArtifactClient class in master branch
{

    void addMetadata(Metadata metadata,
                     String path,
                     String storageId,
                     String repositoryId,
                     InputStream is)
            throws ArtifactOperationException;

    String getContextBaseUrl();

    void deployFile(InputStream is,
                    String url,
                    String fileName)
            throws ArtifactOperationException;

    boolean pathExists(String path);

    InputStream getResource(String path)
            throws ArtifactTransportException,
                   IOException;

    InputStream getResource(String path,
                            long offset)
            throws ArtifactTransportException,
                   IOException;

    void addArtifact(Artifact artifact,
                     String storageId,
                     String repositoryId,
                     InputStream is)
            throws ArtifactOperationException;

    void deployMetadata(InputStream is,
                        String url,
                        String fileName)
            throws ArtifactOperationException;
}
