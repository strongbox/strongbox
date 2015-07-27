package org.carlspring.strongbox.io.reloading;

import org.carlspring.strongbox.resource.ResourceCloser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author mtodorov
 */
public class FSReloadableInputStreamHandler
    implements ReloadableInputStreamHandler
{

    private static final Logger logger = LoggerFactory.getLogger(FSReloadableInputStreamHandler.class);

    private File file;

    private InputStream inputStream;


    public FSReloadableInputStreamHandler(File file)
    {
        this.file = file;
    }

    @Override
    public InputStream getInputStream()
            throws IOException
    {
        if (inputStream == null)
        {
            loadInputStream();
            return inputStream;
        }
        else
        {
            return inputStream;
        }
    }

    @Override
    public void reload()
            throws IOException
    {
        ResourceCloser.close(inputStream, logger);
        loadInputStream();
    }

    private void loadInputStream()
            throws FileNotFoundException
    {
        inputStream = new FileInputStream(file);
    }

}
