package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("mavenIndexerSearchProvider")
public class MavenIndexerSearchProvider
        implements SearchProvider
{

    private static final Logger logger = LoggerFactory.getLogger(MavenIndexerSearchProvider.class);

    public static final String ALIAS = "Maven Indexer";

    @Inject
    private SearchProviderRegistry searchProviderRegistry;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ConfigurationManager configurationManager;


    @PostConstruct
    @Override
    public void register()
    {
        searchProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered search provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws SearchException
    {
        SearchResults searchResults = new SearchResults();

        final String repositoryId = searchRequest.getRepositoryId();

        try
        {
            final Collection<Storage> storages = getConfiguration().getStorages().values();
            if (repositoryId != null && !repositoryId.isEmpty())
            {
                logger.debug("Repository: {}", repositoryId);

                final String storageId = searchRequest.getStorageId();
                if (storageId == null)
                {
                    for (Storage storage : storages)
                    {
                        if (storage.containsRepository(repositoryId))
                        {
                            final String indexType = searchRequest.getOption("indexType") != null ?
                                                     searchRequest.getOption("indexType") :
                                                     IndexTypeEnum.LOCAL.getType();

                            final String contextId = storage.getId() + ":" + repositoryId + ":" + indexType;

                            final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(contextId)
                                                                               .search(searchRequest.getQuery());

                            if (sr != null && !sr.isEmpty())
                            {
                                searchResults.getResults().addAll(sr);
                            }
                        }
                    }

                    logger.debug("Results: {}", searchResults.getResults().size());

                    return searchResults;
                }
                else
                {
                    final String indexType = searchRequest.getOption("indexType") != null ?
                                             searchRequest.getOption("indexType") :
                                             IndexTypeEnum.LOCAL.getType();

                    String contextId = searchRequest.getStorageId() + ":" +
                                       searchRequest.getRepositoryId() + ":" +
                                       indexType;

                    RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);
                    if (indexer != null)
                    {
                        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(contextId)
                                                                           .search(searchRequest.getQuery());

                        if (!sr.isEmpty())
                        {
                            searchResults.getResults().addAll(sr);
                        }
                    }

                    logger.debug("Results: {}", searchResults.getResults().size());

                    return searchResults;
                }
            }
            else
            {
                for (Storage storage : storages)
                {
                    for (Repository r : storage.getRepositories().values())
                    {
                        logger.debug("Repository: {}", r.getId());

                        final String indexType = searchRequest.getOption("indexType") != null ?
                                                 searchRequest.getOption("indexType") :
                                                 IndexTypeEnum.LOCAL.getType();

                        final String contextId = storage.getId() + ":" + r.getId() + ":" + indexType;

                        final RepositoryIndexer
                                repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);
                        if (repositoryIndexer != null)
                        {
                            final Set<SearchResult> sr = repositoryIndexer.search(searchRequest.getQuery());

                            if (sr != null && !sr.isEmpty())
                            {
                                searchResults.getResults().addAll(sr);
                            }
                        }
                    }
                }

                logger.debug("Results: {}", searchResults.getResults().size());

                return searchResults;
            }
        }
        catch (ParseException | IOException e)
        {
            logger.error(e.getMessage(), e);

            throw new SearchException(e.getMessage(), e);
        }
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults().isEmpty();
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public void setRepositoryIndexManager(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
