package org.carlspring.strongbox.services.impl;

import static org.mockito.Matchers.anyString;

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
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
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

    private static final String QUERY_PATTERN_DB = "([\\w]+)=([^;]+);";

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
        // If query matches then we have a DataBase search request
        if (matcher.find())
        {
            Map<String, String> coordinates = new HashMap<>();
            do
            {
                coordinates.put(matcher.group(1), matcher.group(2));
            } while (matcher.find());
            searchResults.getResults()
                         .addAll(dbSearch(coordinates));
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
                Storage storage = getConfiguration().getStorage(storageId);
                searchResults.getResults().addAll(indexSearch(query, storage, repositoryId));
            }
        }
        else
        {
            searchResults.getResults().addAll(indexSearch(query, storages));
        }
        logger.debug("Results: {}", searchResults.getResults().size());
        return searchResults;
    }

    private List<SearchResult> dbSearch(Map<String, String> coordinates)
    {
        List<SearchResult> result = new LinkedList<>();
        result.addAll(artifactEntryService.findByCoordinates(coordinates)
                                          .stream()
                                          .map(a -> createSearchResult(a))
                                          .collect(Collectors.toList()));
        return result;
    }

    protected SearchResult createSearchResult(ArtifactEntry a)
    {
        String storageId = a.getStorageId() + ":" + a.getRepositoryId() + ":" + IndexTypeEnum.LOCAL.getType();
        String url = getURLForArtifact(storageId, a.getRepositoryId(), a.getArtifactCoordinates().toPath());

        return new SearchResult(storageId, a.getRepositoryId(),
                a.getArtifactCoordinates(), url);
    }

    // TODO: [sbespalov] extract this logic into some common utility class
    public String getURLForArtifact(String storageId,
                                    String repositoryId,
                                    String pathToArtifactFile)
    {
        String baseUrl = getConfiguration().getBaseUrl();
        baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");

        return baseUrl + "storages/" + storageId + "/" + repositoryId + "/" + pathToArtifactFile;
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
                final RepositoryIndexer repositoryIndex = repositoryIndexManager.getRepositoryIndexer(getIndexId(storage, r));
                if (repositoryIndex != null)
                {
                    final Set<SearchResult> sr = repositoryIndex.search(query);
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
                                           Storage storage,
                                           String repositoryId)
        throws ParseException,
        IOException
    {
        Repository repository = storage.getRepository(repositoryId);
        String storageAndRepositoryId = getIndexId(storage, repository);
        List<SearchResult> result = new LinkedList<>();
        final Set<SearchResult> sr = repositoryIndexManager.getRepositoryIndexer(storageAndRepositoryId)
                                                           .search(query);
        if (!sr.isEmpty())
        {
            result.addAll(sr);
        }
        return result;
    }

    protected String getIndexId(Storage storage,
                                Repository repository)
    {
        return storage.getId() + ":" + repository.getId() + ":"
                + (repository.isProxyRepository() ? IndexTypeEnum.REMOTE.getType() : IndexTypeEnum.LOCAL.getType());
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
                final String storageAndRepositoryId = getIndexId(storage, storage.getRepository(repositoryId));
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
    
    public static void main(String args[])
        throws Exception
    {
        Pattern pattern = Pattern.compile(QUERY_PATTERN_DB);
        Matcher matcher = pattern.matcher("groupId=org.carlspring.strongbox.searches;artifactId=test-project;");
        // If query matches then we have a DataBase search request
        //System.out.println(matcher.matches());
        Map<String, String> coordinates = new HashMap<>();
        while (matcher.find())
        {
            coordinates.put(matcher.group(1), matcher.group(2));
        }
        System.out.println(coordinates);
    }

}