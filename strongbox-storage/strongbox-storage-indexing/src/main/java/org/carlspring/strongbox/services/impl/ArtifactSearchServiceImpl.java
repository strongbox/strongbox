package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.*;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.util.*;

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

        final String repository = searchRequest.getRepository();

        if (repository != null && !repository.isEmpty())
        {
            logger.debug("Repository: {}", repository);

            final String storage = searchRequest.getStorage();
            if (storage == null)
            {
                List<Storage> storages = dataCenter.getStoragesContainingRepository(repository);

                int results = 0;
                final Map<String, Collection<SearchResult>> resultsMap = new LinkedHashMap<>();
                for (Storage s: storages)
                {
                    final String storageAndRepositoryId = s.getId() + ":" + repository;
                    final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storageAndRepositoryId)
                                                                       .search(searchRequest.getQuery());

                    if (!sr.isEmpty())
                    {
                        resultsMap.put(storageAndRepositoryId, sr);
                        results += sr.size();
                    }
                }

                searchResults.setResults(resultsMap);

                logger.debug("Results: {}", results);
            }
            else
            {
                final Map<String, Collection<SearchResult>> resultsMap = getResultsMap(storage,
                                                                                       repository,
                                                                                       searchRequest.getQuery());

                if (!resultsMap.isEmpty())
                {
                    searchResults.setResults(resultsMap);
                }

                if (logger.isDebugEnabled())
                {
                    int results = resultsMap.entrySet().iterator().next().getValue().size();

                    logger.debug("Results: {}", results);
                }
            }
        }
        else
        {
            Map<String, Collection<SearchResult>> resultsMap = new LinkedHashMap<>();

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

                        if (!sr.isEmpty())
                        {
                            resultsMap.put(storage.getId() + ":" + r.getId(), sr);
                        }

                        logger.debug("Results: {}", sr.size());
                    }
                }
            }

            searchResults.setResults(resultsMap);
        }

        return searchResults;
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws IOException, ParseException
    {
        return !getResultsMap(searchRequest.getStorage(), searchRequest.getRepository(), searchRequest.getQuery()).isEmpty();
    }

    public Map<String, Collection<SearchResult>> getResultsMap(String storageName, String repository, String query)
            throws IOException, ParseException
    {
        Map<String, Collection<SearchResult>> resultsMap = new LinkedHashMap<>();
        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndex(storageName + ":" + repository).search(query);

        if (!sr.isEmpty())
        {
            resultsMap.put(storageName + ":" + repository, sr);
        }

        return resultsMap;
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
