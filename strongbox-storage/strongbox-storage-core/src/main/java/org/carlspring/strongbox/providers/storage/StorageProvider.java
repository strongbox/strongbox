package org.carlspring.strongbox.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;

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
    
    FileSystem getFileSistem();
    
    FileSystemProvider getFileSystemProvider();
}
