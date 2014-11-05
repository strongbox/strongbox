package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.*;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.util.List;
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
public class ArtifactSearchServiceImpl implements ArtifactSearchService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactSearchServiceImpl.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private DataCenter dataCenter;


    @Override
    public SearchResults search(SearchRequest searchRequest)
            throws IOException, ParseException
    {
        SearchResults searchResults = new SearchResults();

        final String repository = searchRequest.getRepositoryId();

        if (repository != null && !repository.isEmpty())
        {
            logger.debug("Repository: {}", repository);

            final String storage = searchRequest.getStorageId();
            if (storage == null)
            {
                List<Storage> storages = dataCenter.getStoragesContainingRepository(repository);
                for (Storage s: storages)
                {
                    final String storageAndRepositoryId = s.getId() + ":" + repository;
                    final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storageAndRepositoryId)
                                                                       .search(searchRequest.getQuery());

                    if (sr != null && !sr.isEmpty())
                    {
                        searchResults.getResults().addAll(sr);
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
            for (Storage storage : dataCenter.getStorages().values())
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

}
