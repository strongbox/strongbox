package org.carlspring.strongbox.storage.indexing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.index.DefaultIndexer;
import org.apache.maven.index.IndexerEngine;
import org.apache.maven.index.QueryCreator;
import org.apache.maven.index.SearchEngine;
import org.apache.maven.index.context.ExistingLuceneIndexMismatchException;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.util.IndexCreatorSorter;

public class StrongboxIndexer extends DefaultIndexer
{

    public StrongboxIndexer(SearchEngine searcher, IndexerEngine indexerEngine, QueryCreator queryCreator)
    {
        super(searcher, indexerEngine, queryCreator);
    }

    @Override
    public IndexingContext createIndexingContext(String id,
                                                 String repositoryId,
                                                 File repository,
                                                 File indexDirectory,
                                                 String repositoryUrl,
                                                 String indexUpdateUrl,
                                                 boolean searchable,
                                                 boolean reclaim,
                                                 List<? extends IndexCreator> indexers)
                                                                                        throws IOException,
                                                                                        ExistingLuceneIndexMismatchException,
                                                                                        IllegalArgumentException
    {
        final IndexingContext context = new StrongboxIndexingContext(id, repositoryId, repository, indexDirectory,
                repositoryUrl, indexUpdateUrl,
                IndexCreatorSorter.sort(indexers), reclaim);
        context.setSearchable(searchable);
        return context;
    }

}
