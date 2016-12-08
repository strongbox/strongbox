package org.carlspring.strongbox.providers.storage;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.ArtifactInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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

    ArtifactInputStream getInputStreamImplementation(String path)
        throws NoSuchAlgorithmException,
        FileNotFoundException;

    ArtifactInputStream getInputStreamImplementation(File file)
        throws NoSuchAlgorithmException,
        FileNotFoundException;

    ArtifactInputStream getInputStreamImplementation(InputStream is,
                                                     String[] algorithms)
        throws NoSuchAlgorithmException;

    ArtifactInputStream getInputStreamImplementation(ArtifactCoordinates coordinates,
                                                     InputStream is)
        throws NoSuchAlgorithmException;

    File getFileImplementation(String path)
        throws IOException;

    File getFileImplementation(String parentPath,
                               String path)
        throws IOException;

    ArtifactOutputStream getOutputStreamImplementation(String basePath,
                                                       ArtifactCoordinates coordinates)
        throws IOException;;

    ArtifactOutputStream getOutputStreamImplementation(String basePath,
                                                       String artifactPath)
        throws IOException;;

}
