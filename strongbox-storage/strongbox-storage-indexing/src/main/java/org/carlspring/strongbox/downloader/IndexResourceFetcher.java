package org.carlspring.strongbox.downloader;

import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.client.ArtifactTransportException;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.index.updater.ResourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class IndexResourceFetcher
        extends ArtifactClient
        implements ResourceFetcher
{

    private static final Logger logger = LoggerFactory.getLogger(IndexResourceFetcher.class);


    @Override
    public void connect(String username,
                        String password)
            throws IOException
    {
        // It seems like we should be ignoring this.
    }

    @Override
    public void disconnect()
            throws IOException
    {
        close();
    }

    @Override
    public InputStream retrieve(String url)
            throws IOException
    {
        logger.debug("Requesting index from " + url + "...");

        InputStream is;
        try
        {
            is = getResource(url);

            if (is != null)
            {
                logger.debug(" > Index download in progress for " + url + "...");
            }
        }
        catch (ArtifactTransportException e)
        {
            logger.error(e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        }

        return is;
    }

}

