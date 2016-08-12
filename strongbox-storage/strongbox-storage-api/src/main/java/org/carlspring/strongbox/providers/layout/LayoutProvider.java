package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;

/**
 * @author carlspring
 */
public interface LayoutProvider
{

    void register();

    String getAlias();

    ArtifactInputStream getInputStream(String storageId, String repositoryId, String path)
            throws IOException, NoSuchAlgorithmException, ArtifactTransportException;

    OutputStream getOutputStream(String storageId, String repositoryId, String path)
            throws IOException;

    boolean containsArtifact(Repository repository, Artifact artifact)
            throws IOException;

    boolean contains(String storageId, String repositoryId, String path)
            throws IOException;

    boolean containsPath(Repository repository, String path)
            throws IOException;

    String getPathToArtifact(Repository repository, Artifact artifact)
            throws IOException;

    void copy(String srcStorageId,
              String srcRepositoryId,
              String destStorageId,
              String destRepositoryId,
              String path)
            throws IOException;

    void move(String srcStorageId,
              String srcRepositoryId,
              String destStorageId,
              String destRepositoryId,
              String path)
            throws IOException;

    void delete(String storageId, String repositoryId, String path, boolean force)
            throws IOException;

    void deleteMetadata(String storageId, String repositoryId, String metadataPath)
            throws IOException;

    void deleteTrash(String storageId, String repositoryId)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void undelete(String storageId, String repositoryId, String path)
            throws IOException;

    void undeleteTrash(String storageId, String repositoryId)
            throws IOException;

    void undeleteTrash()
            throws IOException;

}
