package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactByteStreams
{

    long copy(InputStream inputStream,
              OutputStream outputStream,
              RepositoryPath artifactPath)
            throws IOException;
}
