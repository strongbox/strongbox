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

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexManager.class);

    /**
     * K: storageId:repositoryId:type[local|remote]
     * V: index
     */
    private Map<String, RepositoryIndexer> indexes = new LinkedHashMap<>();

    @Inject
    private ConfigurationManager configurationManager;

    public RepositoryIndexManager()
    {
    }

    @PreDestroy
    private void close()
    {
        indexes.forEach((contextId, repositoryIndexer) ->
                        {
                            try
                            {
                                closeIndexer(contextId, repositoryIndexer, false);
                            }
                            catch (IOException e)
                            {
                                logger.error("Unable to close indexer for contextId " + contextId, e);
                            }
                        });

        indexes.clear();
        indexes = null;
    }

    public void closeIndexersForStorage(String storageId)
    {
        indexes.keySet()
               .stream()
               .filter(contextId -> contextId.substring(0, contextId.indexOf(':'))
                                             .equals(storageId))
               .forEach(contextId ->
                        {
                            try
                            {
                                closeIndexer(contextId);
                            }
                            catch (IOException e)
                            {
                                logger.error(e.getMessage(), e);
                            }
                        });
    }

    public void closeIndexersForRepository(String storageId,
                                           String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (repository.isProxyRepository())
        {
            closeIndexer(storageId + ":" + repositoryId + ":" + IndexTypeEnum.REMOTE.getType());
        }

        closeIndexer(storageId + ":" + repositoryId + ":" + IndexTypeEnum.LOCAL.getType());
    }

    public void closeIndexer(String contextId)
            throws IOException
    {
        closeIndexer(contextId, indexes.get(contextId));
    }

    public void closeIndexer(String contextId,
                             RepositoryIndexer repositoryIndexer)
            throws IOException
    {
        closeIndexer(contextId, repositoryIndexer, true);
    }

    public void closeIndexer(String contextId,
                             RepositoryIndexer repositoryIndexer,
                             boolean remove)
            throws IOException
    {
        logger.debug("Indexes size:" + indexes.size());

        if (repositoryIndexer != null)
        {
            logger.debug("Closing indexer for " + contextId + "...");

            repositoryIndexer.close();

            logger.debug("Closed indexer for " + contextId + ".");
        }

        if (remove)
        {
            indexes.remove(contextId);
        }
    }

    public Map<String, RepositoryIndexer> getIndexes()
    {
        return indexes;
    }

    public void setIndexes(Map<String, RepositoryIndexer> indexes)
    {
        this.indexes = indexes;
    }

    public RepositoryIndexer getRepositoryIndexer(String contextId)
    {
        return indexes.get(contextId);
    }

    public RepositoryIndexer addRepositoryIndexer(String contextId,
                                                  RepositoryIndexer value)
    {
        return indexes.put(contextId, value);
    }

    public RepositoryIndexer removeRepositoryIndexer(String contextId)
    {
        return indexes.remove(contextId);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
