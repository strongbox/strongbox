package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactByteStreamsCopyStrategy
{

    /**
     * Default size of buffers allocated for copies.
     */
    int BUF_SIZE = 8192;

    long copy(InputStream from,
              OutputStream to,
              RepositoryPath artifactPath)
            throws IOException;
}
