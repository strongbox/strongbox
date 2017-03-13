package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryIndexManager")
public class RepositoryIndexManager
{

    /**
     * K: storageId:repositoryId:type[local|remote]
     * V: index
     */
    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexManager.class);

    private Map<String, RepositoryIndexer> indexes = new LinkedHashMap<>();

    @Inject
    private ConfigurationManager configurationManager;


    public RepositoryIndexManager()
    {
    }

    @PreDestroy
    private synchronized void close()
    {
        for (String contextId : indexes.keySet())
        {
            try
            {
                closeIndexer(contextId);
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void closeIndexersForStorage(String storageId)
    {
        for (String contextId : indexes.keySet())
        {
            if (contextId.substring(0, contextId.indexOf(':')).equals(storageId))
            {
                try
                {
                    closeIndexer(contextId);
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void closeIndexersForRepository(String storageId, String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (repository.isProxyRepository())
        {
            closeIndexer(storageId + ":" + repositoryId + ":remote");
        }

        closeIndexer(storageId + ":" + repositoryId + ":local");
    }

    public void closeIndexer(String contextId)
            throws IOException
    {
        final RepositoryIndexer repositoryIndexer = indexes.get(contextId);

        System.out.println("Indexes size:" + indexes.size());

        if (repositoryIndexer != null)
        {
            logger.debug("Closing indexer for " + contextId + "...");

            repositoryIndexer.close();

            logger.debug("Closed indexer for " + contextId + ".");
        }

        indexes.remove(contextId);
    }

    public Map<String, RepositoryIndexer> getIndexes()
    {
        return indexes;
    }

    public void setIndexes(Map<String, RepositoryIndexer> indexes)
    {
        this.indexes = indexes;
    }

    public RepositoryIndexer getRepositoryIndexer(String storageAndRepositoryId)
    {
        return indexes.get(storageAndRepositoryId);
    }

    public RepositoryIndexer addRepositoryIndexer(String repositoryId, RepositoryIndexer value)
    {
        return indexes.put(repositoryId, value);
    }

    public RepositoryIndexer removeRepositoryIndex(String repositoryId)
    {
        return indexes.remove(repositoryId);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
