package org.carlspring.strongbox.storage.indexing.downloader;

import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.index.Indexer;

/**
 * @author carlspring
 */
public class IndexDownloadRequest
{


    private final Indexer indexer;

    private final Repository repository;

    public IndexDownloadRequest(Repository repository,
                                Indexer indexer)
    {
        this.repository = repository;
        this.indexer = indexer;
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public Repository getRepository()
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
        return ((ImmutableRepository)repository).getRemoteRepository().getUrl();
    }
}
