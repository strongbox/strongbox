package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.dependency.snippet.CodeSnippet;
import org.carlspring.strongbox.dependency.snippet.SnippetGenerator;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.AqlSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class AqlSearchServiceImpl implements AqlSearchService
{

    //@PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private SnippetGenerator snippetGenerator;

    public SearchResults search(Selector<ArtifactEntity> selector)
        throws IOException
    {
        SearchResults result = new SearchResults();

        QueryTemplate<List<ArtifactEntity>, ArtifactEntity> queryTemplate = new OQueryTemplate<>(entityManager);
        for (ArtifactEntity artifactEntry : queryTemplate.select(selector))
        {
            SearchResult r = new SearchResult();
            result.getResults().add(r);

            r.setStorageId(artifactEntry.getStorageId());
            r.setRepositoryId(artifactEntry.getRepositoryId());
            r.setArtifactCoordinates(artifactEntry.getArtifactCoordinates());

            RepositoryPath repositoryPath = repositoryPathResolver.resolve(artifactEntry.getStorageId(),
                                                                           artifactEntry.getRepositoryId(),
                                                                           artifactEntry.getArtifactPath());

            Repository repository = repositoryPath.getRepository();

            URL artifactResource = RepositoryFiles.readResourceUrl(repositoryPath);
            r.setUrl(artifactResource.toString());

            List<CodeSnippet> snippets = snippetGenerator.generateSnippets(repository.getLayout(),
                                                                             artifactEntry.getArtifactCoordinates());
            r.setSnippets(snippets);
        }

        return result;
    }

}
