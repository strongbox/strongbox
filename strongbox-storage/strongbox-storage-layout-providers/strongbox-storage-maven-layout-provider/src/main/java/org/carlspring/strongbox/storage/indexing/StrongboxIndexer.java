package org.carlspring.strongbox.storage.indexing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.index.DefaultIndexer;
import org.apache.maven.index.IndexerEngine;
import org.apache.maven.index.QueryCreator;
import org.apache.maven.index.SearchEngine;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.util.IndexCreatorSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongboxIndexer
        extends DefaultIndexer
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxIndexer.class);


    public StrongboxIndexer(SearchEngine searcher,
                            IndexerEngine indexerEngine,
                            QueryCreator queryCreator)
    {
        super(searcher, indexerEngine, queryCreator);
    }

    @Override
    public IndexingContext createIndexingContext(String contextId,
                                                 String repositoryId,
                                                 File repository,
                                                 File indexDirectory,
                                                 String repositoryUrl,
                                                 String indexUpdateUrl,
                                                 boolean searchable,
                                                 boolean reclaim,
                                                 List<? extends IndexCreator> indexers)
            throws IOException,
                   IllegalArgumentException
    {
        final IndexingContext context = new StrongboxIndexingContext(contextId,
                                                                     repositoryId,
                                                                     repository,
                                                                     indexDirectory,
                                                                     repositoryUrl,
                                                                     indexUpdateUrl,
                                                                     IndexCreatorSorter.sort(indexers),
                                                                     reclaim);
        context.setSearchable(searchable);

        logger.debug("Indexing context for " + contextId + " successfully created.");

        return context;
    }

}
