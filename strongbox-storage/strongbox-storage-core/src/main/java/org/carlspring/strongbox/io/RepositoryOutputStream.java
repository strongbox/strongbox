package org.carlspring.strongbox.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.io.output.CountingOutputStream;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.util.Assert;

/**
 * This class decorate {@link ArtifactOutputStream} with {@link Repository} specific logic.
 * 
 * @author Sergey Bespalov
 *
 */
public class RepositoryOutputStream extends FilterOutputStream implements RepositoryStreamContext
{

    protected RepositoryStreamCallback callback;

    private Repository repository;
    private String path;

    protected RepositoryOutputStream(Repository repository,
                                     String path,
                                     OutputStream out)
    {
        super(new CountingOutputStream(out));
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

    
    
    @Override
    public void write(int b)
        throws IOException
    {
        if (((CountingOutputStream)out).getByteCount() == 0L){
            Optional.ofNullable(callback).ifPresent(c -> c.onBeforeWrite(this));
        }
        super.write(b);
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        Optional.ofNullable(callback).ifPresent(c -> c.onAfterClose(this));
    }

    public RepositoryOutputStream with(RepositoryStreamCallback callback)
    {
        this.callback = callback;
        return this;
    }

    public static RepositoryOutputStream of(Repository repository,
                                            String path,
                                            OutputStream os)
    {
        ArtifactOutputStream source = os instanceof ArtifactOutputStream ? (ArtifactOutputStream) os
                : StreamUtils.findSource(ArtifactOutputStream.class, os);
        Assert.notNull(source, String.format("Source should be [%s]", ArtifactOutputStream.class.getSimpleName()));

        return new RepositoryOutputStream(repository, path, os);
    }

}
