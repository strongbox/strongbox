package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.InputStream;

import org.carlspring.commons.http.range.ByteRange;

public class StreamUtils
{

    public static long getLength(ArtifactInputStream is)
        throws IOException
    {
        InputStream target = is.getTarget();
        if (!(target instanceof SbByteRangeInputStream))
        {
            return -1L;
        }
        SbByteRangeInputStream bris = (SbByteRangeInputStream) target;
        return bris.getLength();
    }

    public static void setCurrentByteRange(ArtifactInputStream is,
                                           ByteRange byteRange)
        throws IOException
    {
        InputStream target = is.getTarget();
        if (!(target instanceof SbByteRangeInputStream))
        {
            return;
        }
        SbByteRangeInputStream bris = (SbByteRangeInputStream) target;
        bris.setCurrentByteRange(byteRange);
        bris.skip(byteRange.getOffset());
    }

}
