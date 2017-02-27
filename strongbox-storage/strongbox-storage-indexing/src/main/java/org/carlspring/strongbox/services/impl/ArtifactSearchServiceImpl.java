package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.lucene.queryparser.classic.ParseException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResult;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.storage.repository.Repository;
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

    private static final String QUERY_PATTERN_DB = "([\\w]+)?=([^;]+);?";

    private static final Logger logger = LoggerFactory.getLogger(ArtifactSearchServiceImpl.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ArtifactEntryService artifactEntryService;

    @Override
    public SearchResults search(SearchRequest searchRequest)
        throws IOException,
        ParseException
    {
        SearchResults searchResults = new SearchResults();

        String storageId = searchRequest.getStorageId();
        String repositoryId = searchRequest.getRepositoryId();
        String query = searchRequest.getQuery();

        Pattern pattern = Pattern.compile(QUERY_PATTERN_DB);
        Matcher matcher = pattern.matcher(query);
        //If query matches then we have a DataBase search request
        if (matcher.matches())
        {
            Map<String, String> coordinates = new HashMap<>();
            while (matcher.find())
            {
                coordinates.put(matcher.group(1), matcher.group(2));
            }
            searchResults.getResults()
                         .addAll(dbSearch(coordinates, storageId, repositoryId));
            return searchResults;
        }

        final Collection<Storage> storages = getConfiguration().getStorages().values();
        if (repositoryId != null && !repositoryId.isEmpty())
        {
            logger.debug("Repository: {}", repositoryId);
            if (storageId == null)
            {
                searchResults.getResults().addAll(indexSearch(query, repositoryId, storages));
            }
            else
            {
                searchResults.getResults().addAll(indexSearch(query, storageId, repositoryId));
            }
        }
        else
        {
            searchResults.getResults().addAll(indexSearch(query, storages));
        }
        logger.debug("Results: {}", searchResults.getResults().size());
        return searchResults;
    }

    private List<SearchResult> dbSearch(Map<String, String> coordinates,
                                        String storageId,
                                        String repositoryId)
    {
        List<SearchResult> result = new LinkedList<>();

        result.addAll(artifactEntryService.findByCoordinates(coordinates)
                                          .stream()
                                          .map(a -> new SearchResult(a.getStorageId(), a.getRepositoryId(),
                                                  a.getArtifactCoordinates(), ""))
                                          .collect(Collectors.toList()));
        return result;
    }

    private List<SearchResult> indexSearch(String query,
                                           Collection<Storage> storages)
        throws ParseException,
        IOException
    {
        List<SearchResult> result = new LinkedList<>();
        for (Storage storage : storages)
        {
            for (Repository r : storage.getRepositories().values())
            {
                logger.debug("Repository: {}", r.getId());

                final RepositoryIndexer repositoryIndex = repositoryIndexManager.getRepositoryIndexer(storage.getId()
                        + ":" + r.getId());
                if (repositoryIndex != null)
                {
                    final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(storage.getId() + ":"
                            + r.getId()).search(query);
                    if (sr != null && !sr.isEmpty())
                    {
                        result.addAll(sr);
                    }
                }
            }
        }
        return result;
    }

    private List<SearchResult> indexSearch(String query,
                                           String storageId,
                                           String repositoryId)
        throws ParseException,
        IOException
    {
        String storageAndRepositoryId = storageId + ":" + repositoryId;
        List<SearchResult> result = new LinkedList<>();
        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(storageAndRepositoryId)
                                                           .search(query);
        if (!sr.isEmpty())
        {
            result.addAll(sr);
        }
        return result;
    }

    private List<SearchResult> indexSearch(String query,
                                           String repositoryId,
                                           Collection<Storage> storages)
        throws ParseException,
        IOException
    {
        List<SearchResult> result = new LinkedList<>();
        for (Storage storage : storages)
        {
            if (storage.containsRepository(repositoryId))
            {
                final String storageAndRepositoryId = storage.getId() + ":" + repositoryId;
                final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(storageAndRepositoryId)
                                                                   .search(query);

                if (sr != null && !sr.isEmpty())
                {
                    result.addAll(sr);
                }
            }
        }

        return result;
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
        throws IOException,
        ParseException
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
