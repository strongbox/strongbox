package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.inject.Inject;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.carlspring.strongbox.io.RepositoryStreamCallback;
import org.carlspring.strongbox.io.RepositoryStreamContext;
import org.carlspring.strongbox.io.RepositoryStreamReadContext;
import org.carlspring.strongbox.io.RepositoryStreamWriteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public abstract class RepositoryStreamSupport implements RepositoryStreamCallback
{
    private static final Logger logger = LoggerFactory.getLogger(RepositoryStreamSupport.class);

    private ThreadLocal<RepositoryStreamContext> ctx = new ThreadLocal<RepositoryStreamContext>();

    @Inject
    protected RepositoryPathLock repositoryPathLock;
    
    protected void initContext(RepositoryStreamContext ctx)
    {
        this.ctx.set(ctx);
    }

    protected RepositoryStreamContext getContext()
    {
        return ctx.get();
    }

    private void clearContext()
    {
        ctx.remove();
    }

    private void open()
        throws IOException
    {
        RepositoryStreamContext ctx = getContext();
        if (ctx.isOpened())
        {
            return;
        }

        RepositoryPath path = (RepositoryPath) ctx.getPath();
        logger.info(String.format("Locking [%s]", path));
        
        ReadWriteLock lockSource = repositoryPathLock.lock(path);
        Lock lock;
        if (ctx instanceof RepositoryStreamWriteContext)
        {
            lock = lockSource.writeLock();
        }
        else
        {
            lock = lockSource.readLock();
        }

        ctx.setLock(lock);
        lock.lock();

        logger.info(String.format("Locked [%s]", path));
        
        doOpen(ctx);
    }

    private void doOpen(RepositoryStreamContext ctx)
        throws IOException
    {
        ctx.setOpened(true);

        if (ctx instanceof RepositoryStreamWriteContext)
        {
            onBeforeWrite((RepositoryStreamWriteContext) ctx);
        }
        else
        {
            onBeforeRead((RepositoryStreamReadContext) ctx);
        }
    }

    protected void close()
        throws IOException
    {
        RepositoryStreamContext ctx = getContext();
        if (!ctx.isOpened())
        {
            return;
        }

        try
        {
            doClose(ctx);
        } 
        finally
        {
            ctx.getLock().unlock();
            clearContext();
        }
    }

    private void doClose(RepositoryStreamContext ctx)
        throws IOException
    {
        if (ctx instanceof RepositoryStreamWriteContext)
        {
            onAfterWrite((RepositoryStreamWriteContext) ctx);
        }
        else
        {
            onAfterRead((RepositoryStreamReadContext) ctx);
        }
    }
    
    protected void commit() throws IOException
    {
        commit((RepositoryStreamWriteContext) getContext());
    }

    public class RepositoryOutputStream extends ProxyOutputStream
    {
        protected RepositoryOutputStream(Path path,
                                         OutputStream out)
        {
            super(new CountingOutputStream(out));

            RepositoryStreamWriteContext ctx = new RepositoryStreamWriteContext();
            ctx.setStream(this);
            ctx.setPath(path);

            initContext(ctx);
        }
        
        @Override
        protected void beforeWrite(int n)
            throws IOException
        {
            open();
            
            super.beforeWrite(n);
        }


        public void flush()
            throws IOException
        {
            RepositoryStreamSupport.this.commit();
            super.flush();
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                super.close();
            } 
            finally
            {
                RepositoryStreamSupport.this.close();
            }
        }

    }

    public class RepositoryInputStream
            extends ProxyInputStream
    {

        protected RepositoryInputStream(Path path,
                                        InputStream in)
        {
            super(new CountingInputStream(in));

            RepositoryStreamReadContext ctx = new RepositoryStreamReadContext();
            ctx.setPath(path);
            ctx.setStream(this);

            initContext(ctx);
        }

        @Override
        protected void beforeRead(int n)
            throws IOException
        {
            open();
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                super.close();
            } 
            finally
            {
                RepositoryStreamSupport.this.close();
            }
        }

    }

}
