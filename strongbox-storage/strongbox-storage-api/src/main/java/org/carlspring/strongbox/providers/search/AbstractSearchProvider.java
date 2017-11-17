package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;

import javax.inject.Inject;

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
