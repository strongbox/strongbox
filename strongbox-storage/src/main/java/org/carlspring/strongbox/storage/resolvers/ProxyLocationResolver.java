package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ProxyLocationResolver extends FSLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyLocationResolver.class);

    @Autowired
    private ConfigurationManager configurationManager;

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
        final Map<String, Storage> storages = configurationManager.getConfiguration().getStorages();

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
