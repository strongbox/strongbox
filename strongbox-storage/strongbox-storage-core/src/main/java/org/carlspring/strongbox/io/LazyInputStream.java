package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

/**
 * @author sbespalov
 *
 */
public class LazyInputStream extends InputStream
{

    private static final String ERROR_FAILED_TO_CREATE_INPUT_STREAM = "Failed to create InputStream.";

    private Supplier<? extends InputStream> creator;

    private InputStream in;

    public LazyInputStream(Supplier<? extends InputStream> creator)
    {
        this.creator = creator;
    }

    @Override
    public int read()
        throws IOException
    {
        ensureInitialized();
        return in.read();
    }

    @Override
    public int read(byte[] b)
        throws IOException
    {
        ensureInitialized();
        return in.read(b);
    }

    @Override
    public int read(byte[] b,
                    int off,
                    int len)
        throws IOException
    {
        ensureInitialized();
        return in.read(b, off, len);
    }

    @Override
    public long skip(long n)
        throws IOException
    {
        ensureInitialized();
        return in.skip(n);
    }

    @Override
    public int available()
        throws IOException
    {
        ensureInitialized();
        return in.available();
    }

    @Override
    public void close()
        throws IOException
    {
        if (in == null)
        {
            return;
        }
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset()
        throws IOException
    {
        ensureInitialized();
        in.reset();
    }

    @Override
    public boolean markSupported()
    {
        return in.markSupported();
    }

    protected void ensureInitialized()
        throws IOException
    {
        if (in != null)
        {
            return;
        }

        try
        {
            in = creator.get();
        }
        catch (UndeclaredThrowableException e)
        {
            throw new IOException(ERROR_FAILED_TO_CREATE_INPUT_STREAM, e.getUndeclaredThrowable());
        }
        catch (Exception e)
        {
            throw new IOException(ERROR_FAILED_TO_CREATE_INPUT_STREAM, e);
        } 
        finally
        {
            creator = null;
        }
    }

}
