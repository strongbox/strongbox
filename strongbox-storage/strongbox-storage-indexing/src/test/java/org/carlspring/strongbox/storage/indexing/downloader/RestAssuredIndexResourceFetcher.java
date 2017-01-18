package org.carlspring.strongbox.storage.indexing.downloader;

import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.index.updater.ResourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.lucene.util.IOUtils.close;

/**
 * @author carlspring
 */
public class RestAssuredIndexResourceFetcher
        extends RestAssuredArtifactClient
        implements ResourceFetcher
{

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredIndexResourceFetcher.class);


    @Override
    public void connect(String username, String password)
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
        is = getResource(url);

        if (is != null)
        {
            logger.debug(" > Index download in progress for " + url + "...");
        }

        return is;
    }

}
