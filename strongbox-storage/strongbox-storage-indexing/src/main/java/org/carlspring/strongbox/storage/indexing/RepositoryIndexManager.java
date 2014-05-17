package org.carlspring.strongbox.storage.indexing;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Singleton
@Component
public class RepositoryIndexManager
{

    private Map<String, RepositoryIndexer> indexes = new LinkedHashMap<>();


    public RepositoryIndexManager()
    {
    }

    public Map<String, RepositoryIndexer> getIndexes()
    {
        return indexes;
    }

    public void setIndexes(Map<String, RepositoryIndexer> indexes)
    {
        this.indexes = indexes;
    }

    public RepositoryIndexer getRepositoryIndex(String repositoryId)
    {
        return indexes.get(repositoryId);
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
