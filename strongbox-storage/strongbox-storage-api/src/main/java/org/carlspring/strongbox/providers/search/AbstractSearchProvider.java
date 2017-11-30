package org.carlspring.strongbox.providers.search;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.dependency.snippet.CompatibleDependencyFormatRegistry;
import org.carlspring.strongbox.dependency.snippet.DependencySynonymFormatter;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;

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
    
    @Inject
    private ArtifactResolutionService artifactResolutionService;


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
            URI artifactResource = artifactResolutionService.resolveArtifactResource(searchRequest.getStorageId(),
                                                                                     searchRequest.getRepositoryId(),
                                                                                     searchRequest.getArtifactCoordinates());

            searchResult = new SearchResult(artifactEntry.getStorageId(),
                                            artifactEntry.getRepositoryId(),
                                            searchRequest.getArtifactCoordinates(),
                                            artifactResource.toString());

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
        URI artifactResource = artifactResolutionService.resolveArtifactResource(storageId,
                                                                                 a.getRepositoryId(),
                                                                                 a.getArtifactCoordinates());

        return new SearchResult(storageId,
                                a.getRepositoryId(),
                                a.getArtifactCoordinates(),
                                artifactResource.toString());
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
