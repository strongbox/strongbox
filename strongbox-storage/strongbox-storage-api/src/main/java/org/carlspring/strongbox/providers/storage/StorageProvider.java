package org.carlspring.strongbox.providers.storage;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author carlspring
 */
public interface StorageProvider
{

    String getAlias();

    String getImplementation();

    void register();

    ArtifactInputStream getInputStream(String storageId,
                                       String repositoryId,
                                       String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException;

    OutputStream getOutputStream(String storageId,
                                 String repositoryId,
                                 String path)
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

    void delete(String storageId,
                String repositoryId,
                String path,
                boolean force)
            throws IOException;

    void deleteTrash(String storageId, String repositoryId)
            throws IOException;

    void undelete(String storageId,
                  String repositoryId,
                  String path)
            throws IOException;

    void undeleteTrash(String storageId, String repositoryId)
            throws IOException;

    void deleteMetadata(String storageId,
                        String repositoryId,
                        String metadataPath)
            throws IOException;

    boolean containsArtifact(Repository repository, Artifact artifact);

    boolean containsPath(Repository repository, String path);

    String getPathToArtifact(Repository repository, Artifact artifact);

}
