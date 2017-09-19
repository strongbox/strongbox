package org.carlspring.strongbox.storage.indexing.downloader;

import org.apache.maven.index.Indexer;

/**
 * @author carlspring
 */
public class IndexDownloadRequest
{


    private Indexer indexer;

    private String storageId;

    private String repositoryId;

    private String remoteRepositoryURL;


    public IndexDownloadRequest()
    {
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getRemoteRepositoryURL()
    {
        return remoteRepositoryURL;
    }

    public void setRemoteRepositoryURL(String remoteRepositoryURL)
    {
        this.remoteRepositoryURL = remoteRepositoryURL;
    }

}
