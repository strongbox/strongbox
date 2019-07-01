package org.carlspring.strongbox.storage.indexing.downloader;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;

import org.apache.maven.index.Indexer;

/**
 * @author carlspring
 */
public class IndexDownloadRequest
{


    private final Indexer indexer;

    private final RepositoryData repository;

    public IndexDownloadRequest(RepositoryData repository,
                                Indexer indexer)
    {
        this.repository = repository;
        this.indexer = indexer;
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public RepositoryData getRepository()
    {
        return repository;
    }

    public String getStorageId()
    {
        return repository.getStorage().getId();
    }

    public String getRepositoryId()
    {
        return repository.getId();
    }

    public String getRemoteRepositoryURL()
    {
        return ((Repository)repository).getRemoteRepository().getUrl();
    }
}
