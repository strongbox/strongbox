package org.carlspring.strongbox.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.commons.io.output.CountingOutputStream;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This class decorate {@link ArtifactOutputStream} with {@link MutableRepository} specific logic.
 *
 * @author Sergey Bespalov
 *
 */
public class RepositoryOutputStream extends FilterOutputStream implements RepositoryStreamContext
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryOutputStream.class);

    protected RepositoryStreamCallback callback = new EmptyRepositoryStreamCallback();

    private Path path;

    protected RepositoryOutputStream(Path path,
                                     OutputStream out)
    {
        super(new CountingOutputStream(out));
        this.path = path;
    }

    public Path getPath()
    {
        return path;
    }

    @Override
    public void write(int b)
            throws IOException
    {
        CountingOutputStream counting = (CountingOutputStream)out;
        if (counting.getByteCount() == 0L){
            try
            {
                callback.onBeforeWrite(this);
            }
            catch (Exception e)
            {
                logger.error(String.format("Callback failed for [%s]", path), e);
                throw new IOException(e);
            }
        }
        
        super.write(b);
    }

    @Override
    public void close()
            throws IOException
    {
        super.close();
        try
        {
            callback.onAfterClose(this);
        }
        catch (Exception e)
        {
            logger.error(String.format("Callback failed for [%s]", path), e);
            throw new IOException(e);
        }
    }

    public RepositoryOutputStream with(RepositoryStreamCallback callback)
    {
        this.callback = callback;
        return this;
    }

    public static RepositoryOutputStream of(Path path,
                                            OutputStream os)
    {
        ArtifactOutputStream source = os instanceof ArtifactOutputStream ? (ArtifactOutputStream) os
                                                                         : StreamUtils.findSource(ArtifactOutputStream.class, os);
        Assert.notNull(source, String.format("Source should be [%s]", ArtifactOutputStream.class.getSimpleName()));

        return new RepositoryOutputStream(path, os);
    }

}
