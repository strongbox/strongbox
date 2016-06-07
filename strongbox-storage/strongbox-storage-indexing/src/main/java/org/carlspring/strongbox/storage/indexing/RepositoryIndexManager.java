package org.carlspring.strongbox.storage.indexing;

import javax.annotation.PreDestroy;
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
     * K: storage/Id:repositoryId
     * V: index
     */
    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexManager.class);

    private Map<String, RepositoryIndexer> indexes = new LinkedHashMap<>();

    public RepositoryIndexManager()
    {
    }

    @PreDestroy
    private void close()
    {
        for (String storageAndRepository : indexes.keySet())
        {
            try
            {
                closeIndexer(storageAndRepository);
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void closeIndexersForStorage(String storageId)
    {
        for (String storageAndRepository : indexes.keySet())
        {
            if (storageAndRepository.substring(0, storageAndRepository.indexOf(':')).equals(storageId))
            {
                try
                {
                    closeIndexer(storageAndRepository);
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void closeIndexer(String storageAndRepository)
            throws IOException
    {
        final RepositoryIndexer repositoryIndexer = indexes.get(storageAndRepository);

        logger.debug("Closing indexer for " + repositoryIndexer.getStorageId() + ":" + repositoryIndexer.getRepositoryId() + "...");

        repositoryIndexer.close();

        logger.debug("Closed indexer for " + repositoryIndexer.getStorageId() + ":" + repositoryIndexer.getRepositoryId() + ".");

        indexes.remove(storageAndRepository);
    }

    public Map<String, RepositoryIndexer> getIndexes()
    {
        return indexes;
    }

    public void setIndexes(Map<String, RepositoryIndexer> indexes)
    {
        this.indexes = indexes;
    }

    public RepositoryIndexer getRepositoryIndex(String storageAndRepositoryId)
    {
        return indexes.get(storageAndRepositoryId);
    }

    public RepositoryIndexer addRepositoryIndex(String repositoryId, RepositoryIndexer value)
    {
        return indexes.put(repositoryId, value);
    }

    public RepositoryIndexer removeRepositoryIndex(String repositoryId)
    {
        return indexes.remove(repositoryId);
    }

}
