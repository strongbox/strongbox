package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.RepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author carlspring
 */
public interface LayoutProvider<T extends ArtifactCoordinates>
{

    void register();

    String getAlias();

    T getArtifactCoordinates(String path);

    ArtifactInputStream getInputStream(RepositoryPath path) throws IOException;

    ArtifactOutputStream getOutputStream(RepositoryPath path) throws IOException;
    
    String resolveResourcePath(Repository repository,
                               String path)
        throws IOException;
    
    RepositoryPath resolve(Repository repository,
                           ArtifactCoordinates coordinates)
        throws IOException;

    RepositoryPath resolve(Repository repository)
        throws IOException;

    RepositoryFileSystem getRepositoryFileSystem(Repository repository);
    
    RepositoryFileSystemProvider getProvider(Repository repository);
    
    boolean containsArtifact(Repository repository, ArtifactCoordinates coordinates)
            throws IOException;

    boolean contains(String storageId, String repositoryId, String path)
            throws IOException;

    boolean containsPath(Repository repository, String path)
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
            throws IOException, SearchException;

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

    void archive(String storageId, String repositoryId, String path)
            throws IOException;

    Set<String> getDigestAlgorithmSet();

    void rebuildMetadata(String storageId,
                         String repositoryId,
                         String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException;

    void rebuildIndexes(String storageId,
                        String repositoryId,
                        String basePath,
                        boolean forceRegeneration)
            throws IOException;

    boolean isMetadata(String path);

    boolean isChecksum(RepositoryPath path);

    RepositoryManagementStrategy getRepositoryManagementStrategy();

    ArtifactManagementService getArtifactManagementService();

}
