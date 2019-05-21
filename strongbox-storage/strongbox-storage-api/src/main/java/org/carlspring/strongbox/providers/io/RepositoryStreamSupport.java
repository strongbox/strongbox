package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

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
public class RepositoryStreamSupport
{
    private static final Logger logger = LoggerFactory.getLogger(RepositoryStreamSupport.class);

    private RepositoryStreamContext ctx = new RepositoryStreamContext();

    protected final ReadWriteLock lockSource;
    protected final RepositoryStreamCallback callback;
    
    public RepositoryStreamSupport(ReadWriteLock lockSource,
                                   RepositoryStreamCallback callback)
    {
        this.lockSource = lockSource;
        this.callback = callback;
    }

    protected void initContext(RepositoryStreamContext ctx)
    {
        this.ctx = ctx;
    }

    protected RepositoryStreamContext getContext()
    {
        return ctx;
    }

    private void clearContext()
    {
        ctx = null;
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
        logger.debug(String.format("Locking [%s]", path));
        
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

        logger.debug(String.format("Locked [%s]", path));
        
        doOpen(ctx);
    }

    private void doOpen(RepositoryStreamContext ctx)
        throws IOException
    {
        ctx.setOpened(true);

        if (ctx instanceof RepositoryStreamWriteContext)
        {
            callback.onBeforeWrite((RepositoryStreamWriteContext) ctx);
        }
        else
        {
            callback.onBeforeRead((RepositoryStreamReadContext) ctx);
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
            callback.onAfterWrite((RepositoryStreamWriteContext) ctx);
        }
        else
        {
            callback.onAfterRead((RepositoryStreamReadContext) ctx);
        }
    }
    
    protected void commit() throws IOException
    {
        callback.commit((RepositoryStreamWriteContext) getContext());
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
            logger.debug(String.format("Flushing [%s]", getContext().getPath()));
            super.flush();
            logger.debug(String.format("Flushed [%s]", getContext().getPath()));
            RepositoryStreamSupport.this.commit();
            logger.debug(String.format("Commited [%s]", getContext().getPath()));
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                super.close();
            }
            catch (Exception e) 
            {
                logger.error(String.format("Failed to close [%s].", getContext().getPath()), e);
                
                throw e;
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
