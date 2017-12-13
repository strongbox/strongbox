package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Przemyslaw Fusik
 */
public class SimpleArtifactByteStreamsCopy
        implements ArtifactByteStreamsCopyStrategy
{

    public static final ArtifactByteStreamsCopyStrategy INSTANCE = new SimpleArtifactByteStreamsCopy();

    private SimpleArtifactByteStreamsCopy()
    {
    }

    @Override
    public long copy(final InputStream from,
                     final OutputStream to,
                     final RepositoryPath artifactPath)
            throws IOException
    {
        long total = 0;
        try
        {
            byte[] buf = new byte[BUF_SIZE];
            while (true)
            {
                int r = from.read(buf);
                if (r == -1)
                {
                    break;
                }
                to.write(buf, 0, r);
                total += r;
            }
            return total;
        }
        catch (IOException ex)
        {
            throw new ArtifactByteStreamsCopyException(total, ex);
        }
    }
}
