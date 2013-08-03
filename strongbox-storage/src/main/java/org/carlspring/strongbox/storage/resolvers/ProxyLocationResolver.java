package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ProxyLocationResolver extends FSLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyLocationResolver.class);

    private DataCenter dataCenter = new DataCenter();


    @Override
    public InputStream getInputStream(String repository,
                                      String path)
            throws IOException
    {
        // TODO: Check if the file exists.
        final InputStream is = super.getInputStream(repository, path);
        if (is != null)
        {
            // TODO: If it exists, serve it's stream.
            return is;
        }
        else
        {
            // TODO: If not, check the remote's index.

//            if (existsInIndexes(artifact))
//            {
//                // TODO: Return the stream of the locally downloaded file.
//
//            }

        }


        return null;
    }

    @Override
    public void initialize()
            throws IOException
    {
        final Configuration configuration = ConfigurationManager.getInstance().getConfiguration();

        final Map<String, Storage> storages = configuration.getStorages();

        dataCenter.setStorages(storages);

        logger.info("Initialized ProxyLocationResolver.");
    }

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
