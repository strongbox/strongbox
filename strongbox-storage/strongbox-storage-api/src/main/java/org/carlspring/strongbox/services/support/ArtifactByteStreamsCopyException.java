package org.carlspring.strongbox.services.support;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactByteStreamsCopyException
        extends IOException
{

    private final long offset;

    public ArtifactByteStreamsCopyException(final long offset,
                                            final IOException cause)
    {
        super(cause);
        this.offset = offset;
    }

    public long getOffset()
    {
        return offset;
    }
}
