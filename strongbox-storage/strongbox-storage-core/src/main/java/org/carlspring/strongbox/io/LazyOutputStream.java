package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

public class LazyOutputStream extends OutputStream
{
    private static final String ERROR_FAILED_TO_CREATE_OUTPUT_STREAM = "Failed to create OutputStream.";

    private Supplier<? extends OutputStream> creator;

    private OutputStream out;

    public LazyOutputStream(Supplier<? extends OutputStream> creator)
    {
        this.creator = creator;
    }

    @Override
    public void write(int b)
        throws IOException
    {
        ensureInitialized();
        out.write(b);
    }

    @Override
    public void write(byte[] b)
        throws IOException
    {
        ensureInitialized();
        out.write(b);
    }

    @Override
    public void write(byte[] b,
                      int off,
                      int len)
        throws IOException
    {
        ensureInitialized();
        out.write(b, off, len);
    }

    @Override
    public void flush()
        throws IOException
    {
        ensureInitialized();
        out.flush();
    }

    @Override
    public void close()
        throws IOException
    {
        if (out != null)
        {
            out.close();
        }
    }

    protected void ensureInitialized()
        throws IOException
    {
        if (out != null)
        {
            return;
        }

        try
        {
            out = creator.get();
        }
        catch (UndeclaredThrowableException e)
        {
            throw new IOException(ERROR_FAILED_TO_CREATE_OUTPUT_STREAM, e.getUndeclaredThrowable());
        }
        catch (Exception e)
        {
            throw new IOException(ERROR_FAILED_TO_CREATE_OUTPUT_STREAM, e);
        } 
        finally
        {
            creator = null;
        }
    }
}
