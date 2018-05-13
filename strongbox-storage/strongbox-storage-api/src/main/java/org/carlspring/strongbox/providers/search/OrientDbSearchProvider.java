package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;

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
public class OrientDbSearchProvider
        extends AbstractSearchProvider
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbSearchProvider.class);

    private static final String QUERY_PATTERN_DB = "([^=]+)=([^;]+);";

    public static final String ALIAS = "OrientDB";

    @Inject
    private ArtifactEntryService artifactEntryService;

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

        if (matcher.find())
        {
            Map<String, String> coordinates = new HashMap<>();
            do
            {
                coordinates.put(matcher.group(1), matcher.group(2));
            }
            while (matcher.find());

            List<SearchResult> results = new LinkedList<>();
            results.addAll(artifactEntryService.findArtifactList(searchRequest.getStorageId(),
                                                                 searchRequest.getRepositoryId(),
                                                                 coordinates, false)
                                               .stream()
                                               .map(this::createSearchResult)
                                               .collect(Collectors.toList()));

            searchResults.getResults().addAll(results);

            return searchResults;
        }

        logger.debug("Results: {}", searchResults.getResults().size());

        return searchResults;
    }

}
