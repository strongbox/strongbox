package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author mtodorov
 */
@Component
public class ArtifactSearchServiceImpl
        implements ArtifactSearchService, ConfigurationService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactSearchServiceImpl.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private ConfigurationManager configurationManager;


    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws IOException, ParseException
    {
        SearchResults searchResults = new SearchResults();

        final String repositoryId = searchRequest.getRepositoryId();

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
                        final String contextId = getContextId(storage.getId(),
                                                              repositoryId,
                                                              IndexTypeEnum.LOCAL.getType());

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
                final String contextId = getContextId(searchRequest.getStorageId(),
                                                      searchRequest.getRepositoryId(),
                                                      IndexTypeEnum.LOCAL.getType());

                final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(contextId)
                                                                   .search(searchRequest.getQuery());

                if (!sr.isEmpty())
                {
                    searchResults.getResults().addAll(sr);
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

                    final String contextId = getContextId(storage.getId(), r.getId(), IndexTypeEnum.LOCAL.getType());

                    final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);
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

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws IOException, ParseException
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

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}