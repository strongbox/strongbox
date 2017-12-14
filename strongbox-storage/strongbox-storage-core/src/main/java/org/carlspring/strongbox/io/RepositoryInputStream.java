package org.carlspring.strongbox.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.input.CountingInputStream;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.util.Assert;

public class RepositoryInputStream extends FilterInputStream implements RepositoryStreamContext
{

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
            Optional.ofNullable(callback).ifPresent(c -> c.onBeforeRead(this));
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
