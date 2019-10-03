package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.context.IndexingContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
public abstract class BaseRepositoryIndexCreatorTest
{
    protected static final String STORAGE0 = "storage0";

    protected static final org.apache.maven.index.Indexer indexer = Indexer.INSTANCE;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    protected static class RepositoryIndexingContextAssert
            implements Closeable
    {

        private final IndexingContext indexingContext;

        public RepositoryIndexingContextAssert(Repository repository,
                                               RepositoryIndexCreator repositoryIndexCreator,
                                               RepositoryIndexingContextFactory indexingContextFactory)
                throws IOException
        {
            RepositoryPath indexPath = repositoryIndexCreator.apply(repository);
            indexingContext = indexingContextFactory.create(repository);
            indexingContext.merge(new SimpleFSDirectory(indexPath));
        }

        public QueriedIndexerAssert onSearchQuery(Query query)
        {
            return new QueriedIndexerAssert(query, indexingContext);
        }

        public void close()
                throws IOException
        {
            indexingContext.close(true);
        }

        public static class QueriedIndexerAssert
        {

            private final Query query;
            private final IndexingContext indexingContext;

            QueriedIndexerAssert(Query query,
                                 IndexingContext indexingContext)
            {
                this.query = query;
                this.indexingContext = indexingContext;
            }

            public void hitTotalTimes(int expectedHitsCount)
                    throws IOException
            {
                FlatSearchResponse response = indexer.searchFlat(
                        new FlatSearchRequest(query, indexingContext));
                assertThat(response.getTotalHitsCount()).isEqualTo(expectedHitsCount);
            }
        }
    }
}
