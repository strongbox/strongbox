package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.dependency.snippet.CompatibleDependencyFormatRegistry;
import org.carlspring.strongbox.dependency.snippet.DependencySynonymFormatter;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author carlspring
 */
public abstract class AbstractSearchProvider
        implements SearchProvider
{

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @Override
    public SearchResult findExact(SearchRequest searchRequest)
    {
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(searchRequest.getStorageId(),
                                                                           searchRequest.getRepositoryId(),
                                                                           searchRequest.getArtifactCoordinates().toPath())
                                                          .orElse(null);

        SearchResult searchResult = null;
        if (artifactEntry != null)
        {
            String url = artifactEntryService.constructArtifactURL(searchRequest.getStorageId(),
                                                                   searchRequest.getRepositoryId(),
                                                                   searchRequest.getArtifactCoordinates());

            searchResult = new SearchResult(artifactEntry.getStorageId(),
                                            artifactEntry.getRepositoryId(),
                                            searchRequest.getArtifactCoordinates(),
                                            url);

            Storage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
            Repository repository = storage.getRepository(searchRequest.getRepositoryId());

            Map<String, DependencySynonymFormatter> implementations = compatibleDependencyFormatRegistry.getProviderImplementations(repository.getLayout());

            Map<String, String> snippets = new LinkedHashMap<>();
            for (String compatibleDependencyFormat : implementations.keySet())
            {
                DependencySynonymFormatter formatter = implementations.get(compatibleDependencyFormat);

                snippets.put(compatibleDependencyFormat,
                             formatter.getDependencySnippet(searchRequest.getArtifactCoordinates()));
            }

            searchResult.setSnippets(snippets);
        }

        return searchResult;
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults().isEmpty();
    }

    SearchResult createSearchResult(ArtifactEntry a)
    {
        String storageId = a.getStorageId();
        String url = artifactEntryService.constructArtifactURL(storageId,
                                                               a.getRepositoryId(),
                                                               a.getArtifactCoordinates());

        return new SearchResult(storageId,
                                a.getRepositoryId(),
                                a.getArtifactCoordinates(),
                                url);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
