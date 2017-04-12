package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("orientDbSearchProvider")
public class OrientDbSearchProvider implements SearchProvider
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbSearchProvider.class);

    private static final String QUERY_PATTERN_DB = "([\\w]+)=([^;]+);";

    public static final String ALIAS = "OrientDB";

    @Inject
    private SearchProviderRegistry searchProviderRegistry;

    @Inject
    private ArtifactEntryService artifactEntryService;

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

        String query = searchRequest.getQuery();

        Pattern pattern = Pattern.compile(QUERY_PATTERN_DB);
        Matcher matcher = pattern.matcher(query);

        // TODO: Sergey:
        // TODO: Sergey: I think this is wrong in (at least) the following ways:
        // TODO: Sergey: 1) What if there is just one parameter?
        // TODO: Sergey: 2) The syntax is not clear to anyone, nor is it documented
        // TODO: Sergey:
        if (matcher.find())
        {
            Map<String, String> coordinates = new HashMap<>();
            do
            {
                coordinates.put(matcher.group(1), matcher.group(2));
            } while (matcher.find());

            List<SearchResult> results = new LinkedList<>();
            results.addAll(artifactEntryService.findByCoordinates(coordinates)
                                               .stream()
                                               .map(this::createSearchResult)
                                               .collect(Collectors.toList()));

            searchResults.getResults().addAll(results);

            return searchResults;
        }

        logger.debug("Results: {}", searchResults.getResults().size());

        return searchResults;
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults().isEmpty();
    }

    protected SearchResult createSearchResult(ArtifactEntry a)
    {
        String storageId = a.getStorageId();
        String url = getURLForArtifact(storageId, a.getRepositoryId(), a.getArtifactCoordinates().toPath());

        return new SearchResult(storageId, a.getRepositoryId(),
                                a.getArtifactCoordinates(), url);
    }

    // TODO: Sergey: Extract this logic into some common utility class
    public String getURLForArtifact(String storageId,
                                    String repositoryId,
                                    String pathToArtifactFile)
    {
        String baseUrl = getConfiguration().getBaseUrl();
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        return baseUrl + "storages/" + storageId + "/" + repositoryId + "/" + pathToArtifactFile;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
