package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResult;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                        final String storageAndRepositoryId = storage.getId() + ":" + repositoryId;
                        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storageAndRepositoryId)
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
                String storageAndRepositoryId = searchRequest.getStorageId() + ":" + searchRequest.getRepositoryId();
                final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storageAndRepositoryId)
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

                    final RepositoryIndexer repositoryIndex = repositoryIndexManager.getRepositoryIndex(storage.getId() + ":" + r.getId());
                    if (repositoryIndex != null)
                    {
                        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storage.getId() + ":" + r.getId())
                                                                           .search(searchRequest.getQuery());

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
