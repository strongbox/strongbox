package org.carlspring.strongbox.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author carlspring
 */
public interface StorageProvider
{

    String getAlias();

    void register();

    InputStream getInputStreamImplementation(Path artifactPath)
        throws IOException,
        NoSuchAlgorithmException;

    InputStream getInputStreamImplementation(Path repositoryPath,
                                             String path)
        throws IOException,
        NoSuchAlgorithmException;

    OutputStream getOutputStreamImplementation(Path artifactPath)
        throws IOException,
        NoSuchAlgorithmException;

    OutputStream getOutputStreamImplementation(Path repositoryPath,
                                               String path)
        throws IOException;

    Path resolve(Repository repository,
                 ArtifactCoordinates coordinates)
        throws IOException;

    Path resolve(Repository repository)
        throws IOException;

    Path resolve(Repository repository,
                 String path)
        throws IOException;
}
