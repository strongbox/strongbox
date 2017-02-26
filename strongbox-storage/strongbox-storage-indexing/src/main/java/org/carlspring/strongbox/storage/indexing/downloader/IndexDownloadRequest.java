package org.carlspring.strongbox.storage.indexing.downloader;

import java.io.File;

import org.apache.maven.index.Indexer;

/**
 * @author carlspring
 */
public class IndexDownloadRequest
{


    private Indexer indexer;

    private String indexingContextId;

    private File indexLocalCacheDir;

    private String indexDir;

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

    public String getIndexingContextId()
    {
        return indexingContextId;
    }

    public void setIndexingContextId(String indexingContextId)
    {
        this.indexingContextId = indexingContextId;
    }

    public File getIndexLocalCacheDir()
    {
        return indexLocalCacheDir;
    }

    public void setIndexLocalCacheDir(File indexLocalCacheDir)
    {
        this.indexLocalCacheDir = indexLocalCacheDir;
    }

    public String getIndexDir()
    {
        return indexDir;
    }

    public void setIndexDir(String indexDir)
    {
        this.indexDir = indexDir;
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
