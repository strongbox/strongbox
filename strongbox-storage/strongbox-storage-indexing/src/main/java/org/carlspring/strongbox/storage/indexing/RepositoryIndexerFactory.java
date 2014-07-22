package org.carlspring.strongbox.storage.indexing;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.index.Indexer;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;

/**
 * @author mtodorov
 */
@Singleton
public class RepositoryIndexerFactory
{

    private RepositoryIndexingContext repositoryIndexingContext;


    @Inject
    public RepositoryIndexerFactory(RepositoryIndexingContext repositoryIndexingContext)
    {
        this.repositoryIndexingContext = repositoryIndexingContext;
    }

    public RepositoryIndexer createRepositoryIndexer(String repositoryId,
                                                     File repositoryBasedir,
                                                     File indexDir)
            throws IOException
    {
        RepositoryIndexer repositoryIndexer = new RepositoryIndexer();
        repositoryIndexer.setRepositoryId(repositoryId);
        repositoryIndexer.setRepositoryBasedir(repositoryBasedir);
        repositoryIndexer.setIndexDir(indexDir);
        repositoryIndexer.initialize();

        return repositoryIndexer;
    }

    public RepositoryIndexingContext getRepositoryIndexingContext()
    {
        return repositoryIndexingContext;
    }

    public void setRepositoryIndexingContext(RepositoryIndexingContext repositoryIndexingContext)
    {
        this.repositoryIndexingContext = repositoryIndexingContext;
    }

    public Indexer getIndexer()
    {
        return repositoryIndexingContext.getIndexer();
    }

    public Scanner getScanner()
    {
        return repositoryIndexingContext.getScanner();
    }

    public Map<String, IndexCreator> getIndexers()
    {
        return repositoryIndexingContext.getIndexers();
    }

}
