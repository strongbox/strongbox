package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.carlspring.strongbox.data.criteria.DetachQueryTemplate;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.dependency.snippet.SnippetGenerator;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.AqlSearchService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class AqlSearchServiceImpl implements AqlSearchService
{

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private SnippetGenerator snippetGenerator;

    public SearchResults search(Selector<ArtifactEntry> selector)
        throws IOException
    {
        SearchResults result = new SearchResults();

        QueryTemplate<List<ArtifactEntry>, ArtifactEntry> queryTemplate = new DetachQueryTemplate<>(
                new OQueryTemplate<>(entityManager));
        for (ArtifactEntry artifactEntry : queryTemplate.select(selector))
        {
            SearchResult r = new SearchResult();
            result.getResults().add(r);

            r.setStorageId(artifactEntry.getStorageId());
            r.setRepositoryId(artifactEntry.getRepositoryId());
            r.setArtifactCoordinates(artifactEntry.getArtifactCoordinates());

            RepositoryPath repositoryPath = artifactResolutionService.resolvePath(artifactEntry.getStorageId(),
                                                                                  artifactEntry.getRepositoryId(),
                                                                                  artifactEntry.getArtifactPath());

            Repository repository = repositoryPath.getRepository();

            URL artifactResource = artifactResolutionService.resolveResource(repositoryPath);
            r.setUrl(artifactResource.toString());

            Map<String, String> snippets = snippetGenerator.generateSnippets(repository.getLayout(),
                                                                             artifactEntry.getArtifactCoordinates());
            r.setSnippets(snippets);
        }

        return result;
    }

}