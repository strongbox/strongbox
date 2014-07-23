package org.carlspring.strongbox.storage.indexing;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.index.Indexer;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;

/**
 * @author mtodorov
 */
@Named
@Singleton
public class RepositoryIndexingContext
{

    private Indexer indexer;

    private Scanner scanner;

    private Map<String, IndexCreator> indexers;


    @Inject
    public RepositoryIndexingContext(Indexer indexer,
                                     Scanner scanner,
                                     Map<String, IndexCreator> indexers)
    {
        this.indexer = indexer;
        this.scanner = scanner;
        this.indexers = indexers;
    }

    public List<IndexCreator> getIndexersAsList()
    {
        List<IndexCreator> indexersAsList = new ArrayList<>();
        indexersAsList.add(getIndexers().get("min"));
        indexersAsList.add(getIndexers().get("jarContent"));
        indexersAsList.add(getIndexers().get("maven-plugin"));

        return indexersAsList;
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }

    public Scanner getScanner()
    {
        return scanner;
    }

    public void setScanner(Scanner scanner)
    {
        this.scanner = scanner;
    }

    public Map<String, IndexCreator> getIndexers()
    {
        return indexers;
    }

    public void setIndexers(Map<String, IndexCreator> indexers)
    {
        this.indexers = indexers;
    }

}
