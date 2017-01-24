package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.AbstractByteRangeInputStream;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;

public class ByteRangeInputStream extends AbstractByteRangeInputStream
{

    private long length;

    public ByteRangeInputStream(InputStream is)
        throws NoSuchAlgorithmException
    {
        super(is);
    }

    public ByteRangeInputStream(ReloadableInputStreamHandler handler,
                                ByteRange byteRange)
        throws IOException,
        NoSuchAlgorithmException
    {
        super(handler, byteRange);
    }

    public ByteRangeInputStream(ReloadableInputStreamHandler handler,
                                List<ByteRange> byteRanges)
        throws IOException,
        NoSuchAlgorithmException
    {
        super(handler, byteRanges);
    }

    @Override
    public void reposition(long skipBytes)
        throws IOException
    {
        //Do noting here
    }

    @Override
    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    @Override
    public int read(byte[] bytes,
                    int off,
                    int len)
        throws IOException
    {
        if (hasReachedLimit())
        {
            return -1;
        }

        int numberOfBytesRead = in.read(bytes, off, len);
        if (limit > 0 && bytesRead < limit)
        {
            bytesRead += numberOfBytesRead;
        }

        return numberOfBytesRead;
    }

    @Override
    public int read(byte[] bytes)
        throws IOException
    {
        if (hasReachedLimit())
        {
            return -1;
        }

        int len = in.read(bytes);

        bytesRead += len;

        if (limit > 0 && bytesRead < limit)
        {
            bytesRead += len;
        }

        return len;
    }

}
