package org.carlspring.strongbox.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactPath;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author carlspring
 */
public interface StorageProvider
{

    String getAlias();

    void register();

    InputStream getInputStreamImplementation(ArtifactPath artifactPath)
            throws IOException, NoSuchAlgorithmException;
    
    InputStream getInputStreamImplementation(RepositoryPath repositoryPath, String path)
            throws IOException, NoSuchAlgorithmException;
    
    
    OutputStream getOutputStreamImplementation(ArtifactPath artifactPath)
        throws IOException, NoSuchAlgorithmException;

    OutputStream getOutputStreamImplementation(RepositoryPath repositoryPath, String path)
            throws IOException;
        
    ArtifactPath resolve(Repository repository,
                         ArtifactCoordinates coordinates)
            throws IOException;

    RepositoryPath resolve(Repository repository)
            throws IOException;
    
    RepositoryPath resolve(Repository repository, String path)
            throws IOException;
}
