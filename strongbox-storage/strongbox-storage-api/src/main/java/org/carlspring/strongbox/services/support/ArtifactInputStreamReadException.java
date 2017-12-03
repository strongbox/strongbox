package org.carlspring.strongbox.services.support;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactInputStreamReadException
        extends RuntimeException
{

    private final IOException source;

    private final long bytesReadLength;


    public ArtifactInputStreamReadException(final IOException source,
                                            final long bytesReadLength)
    {
        super(source);
        this.source = source;
        this.bytesReadLength = bytesReadLength;
    }
}
