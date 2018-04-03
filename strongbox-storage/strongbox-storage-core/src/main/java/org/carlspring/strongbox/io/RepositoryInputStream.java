package org.carlspring.strongbox.io;

import org.carlspring.strongbox.storage.repository.Repository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class RepositoryInputStream
        extends BufferedInputStream
        implements RepositoryStreamContext
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryInputStream.class);

    protected RepositoryStreamCallback callback;

    private Repository repository;
    private String path;

    protected RepositoryInputStream(Repository repository,
                                    String path,
                                    InputStream in)
    {
        super(new CountingInputStream(in));
        this.repository = repository;
        this.path = path;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public String getPath()
    {
        return path;
    }

    public RepositoryInputStream with(RepositoryStreamCallback callback)
    {
        this.callback = callback;
        return this;
    }

    @Override
    public int read()
        throws IOException
    {
        if (getBytesCount() == 0l)
        {
            try
            {
                Optional.ofNullable(callback).ifPresent(c -> c.onBeforeRead(this));
            }
            catch (Exception e)
            {
                logger.error(String.format("Callback failed for [%s]", path), e);
                throw new IOException(e);
            }
        }
        return super.read();
    }

    public long getBytesCount()
    {
        return ((CountingInputStream) this.in).getByteCount();
    }

    public static RepositoryInputStream of(Repository repository,
                                           String path,
                                           InputStream is)
    {
        ArtifactInputStream source = is instanceof ArtifactInputStream ? (ArtifactInputStream) is
                : StreamUtils.findSource(ArtifactInputStream.class, is);
        Assert.notNull(source, String.format("Source should be [%s]", ArtifactInputStream.class.getSimpleName()));

        return new RepositoryInputStream(repository, path, is);
    }

}
