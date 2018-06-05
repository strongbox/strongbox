package org.carlspring.strongbox.io;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class RepositoryInputStream
        extends BufferedInputStream
        implements RepositoryStreamContext
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryInputStream.class);

    protected RepositoryStreamCallback callback = new EmptyRepositoryStreamCallback();

    private Path path;

    protected RepositoryInputStream(Path path,
                                    InputStream in)
    {
        super(new CountingInputStream(in));
        this.path = path;
    }

    public Path getPath()
    {
        return path;
    }

    public RepositoryInputStream with(RepositoryStreamCallback callback)
    {
        this.callback = callback;
        return this;
    }

    @Override
    public synchronized int read(byte[] b,
                                 int off,
                                 int len)
        throws IOException
    {
        onRead();
        return super.read(b, off, len);
    }

    @Override
    public int read()
        throws IOException
    {
        onRead();
        return super.read();
    }

    private void onRead()
        throws IOException
    {
        if (getBytesCount() == 0l)
        {
            try
            {
                callback.onBeforeRead(this);
            }
            catch (Exception e)
            {
                logger.error(String.format("Callback failed for [%s]", path), e);
                throw new IOException(e);
            }
        }
    }

    public long getBytesCount()
    {
        return ((CountingInputStream) this.in).getByteCount();
    }

    public static RepositoryInputStream of(Path path,
                                           InputStream is)
    {
        ArtifactInputStream source = is instanceof ArtifactInputStream ? (ArtifactInputStream) is
                : StreamUtils.findSource(ArtifactInputStream.class, is);
        Assert.notNull(source, String.format("Source should be [%s]", ArtifactInputStream.class.getSimpleName()));

        return new RepositoryInputStream(path, is);
    }

}
