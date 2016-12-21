package org.carlspring.strongbox.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
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

    ArtifactInputStream getInputStreamImplementation(ReloadableInputStreamHandler handler,
                                                     List<ByteRange> byteRanges)
        throws IOException,
        NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(ReloadableInputStreamHandler handler,
                                                     ByteRange byteRange)
        throws IOException,
        NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(InputStream is)
        throws NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(InputStream is,
                                                     String[] algorithms)
        throws NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(ArtifactCoordinates coordinates,
                                                     InputStream is)
        throws NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(ArtifactPath artifactPath)
            throws IOException, NoSuchAlgorithmException;
    
    ArtifactInputStream getInputStreamImplementation(RepositoryPath repositoryPath, String path)
            throws IOException, NoSuchAlgorithmException;
    
    
    ArtifactOutputStream getOutputStreamImplementation(ArtifactPath artifactPath)
        throws IOException;;

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
