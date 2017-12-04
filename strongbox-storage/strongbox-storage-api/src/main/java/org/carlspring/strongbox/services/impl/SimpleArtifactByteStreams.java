package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;

/**
 * @author Przemyslaw Fusik
 */
public class SimpleArtifactByteStreams
        implements ArtifactByteStreams
{

    public static final ArtifactByteStreams INSTANCE = new SimpleArtifactByteStreams();

    private SimpleArtifactByteStreams()
    {
    }

    @Override
    public long copy(final InputStream inputStream,
                     final OutputStream outputStream,
                     final RepositoryPath artifactPath)
            throws IOException
    {
        return ByteStreams.copy(inputStream, outputStream);
    }
}
