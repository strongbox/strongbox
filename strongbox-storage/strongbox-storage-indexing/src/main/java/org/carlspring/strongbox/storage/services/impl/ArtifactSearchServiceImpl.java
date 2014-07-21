package org.carlspring.strongbox.storage.services.impl;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.services.ArtifactSearchService;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.index.ArtifactInfo;
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
                final Map<String, Collection<ArtifactInfo>> resultsMap = new LinkedHashMap<>();
                for (Storage s: storages)
                {
                    final String storageAndRepositoryId = s.getName() + ":" + repository;
                    final Set<ArtifactInfo> artifactInfoResults = repositoryIndexManager.getRepositoryIndex(storageAndRepositoryId)
                                                                                        .search(searchRequest.getQuery());

                    if (!artifactInfoResults.isEmpty())
                    {
                        resultsMap.put(storageAndRepositoryId, artifactInfoResults);
                        results += artifactInfoResults.size();
                    }
                }

                searchResults.setResults(resultsMap);

                logger.debug("Results: {}", results);
            }
            else
            {
                final Map<String, Collection<ArtifactInfo>> resultsMap = getResultsMap(storage, repository,
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
            Map<String, Collection<ArtifactInfo>> resultsMap = new LinkedHashMap<>();

            for (Storage storage : dataCenter.getStorages().values())
            {
                for (Repository r : storage.getRepositories().values())
                {
                    logger.debug("Repository: {}", r.getName());

                    final RepositoryIndexer repositoryIndex = repositoryIndexManager.getRepositoryIndex(storage.getName() + ":" + r.getName());
                    if (repositoryIndex != null)
                    {
                        final Set<ArtifactInfo> artifactInfoResults = repositoryIndexManager.getRepositoryIndex(storage.getName() + ":" + r.getName())
                                                                                            .search(searchRequest.getQuery());

                        if (!artifactInfoResults.isEmpty())
                        {
                            resultsMap.put(storage.getName() + ":" + r.getName(), artifactInfoResults);
                        }

                        logger.debug("Results: {}", artifactInfoResults.size());
                    }
                }
            }

            searchResults.setResults(resultsMap);
        }

        return searchResults;
    }

    public Map<String, Collection<ArtifactInfo>> getResultsMap(String storageName, String repository, String query)
            throws IOException, ParseException
    {
        Map<String, Collection<ArtifactInfo>> resultsMap = new LinkedHashMap<>();
        final Set<ArtifactInfo> artifactInfoResults = repositoryIndexManager.getRepositoryIndex(storageName + ":" + repository)
                                                                            .search(query);

        if (!artifactInfoResults.isEmpty())
        {
            resultsMap.put(storageName + ":" + repository, artifactInfoResults);
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
