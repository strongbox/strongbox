package org.carlspring.strongbox.controllers.aql;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.aql.grammar.AqlQueryParser;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author sbespalov
 *
 */
@Controller
@RequestMapping("/api/aql")
@Api(value = "/api/aql")
public class AqlController extends BaseController
{
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @ApiOperation(value = "Used to search for artifacts.", response = SearchResults.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    @PreAuthorize("hasAuthority('SEARCH_ARTIFACTS')")
    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity search(@ApiParam(value = "Search query", required = true) @RequestParam(name = "query", required = true) String query)
        throws IOException,
        SearchException
    {
        SearchResults result = new SearchResults();

        AqlQueryParser parser = new AqlQueryParser(query);
        Selector<ArtifactEntry> selector;
        try
        {
            selector = parser.parseQuery();
        }
        catch (QueryParserException e)
        {
            // TODO: provide response error message

            return ResponseEntity.badRequest().build();
        }

        OQueryTemplate<List<ArtifactEntry>, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
        for (ArtifactEntry artifactEntry : queryTemplate.select(selector))
        {
            SearchResult r = new SearchResult();
            result.getResults().add(r);

            r.setStorageId(artifactEntry.getStorageId());
            r.setStorageId(artifactEntry.getRepositoryId());
            r.setArtifactCoordinates(artifactEntry.getArtifactCoordinates());

            RepositoryPath repositoryPath = artifactResolutionService.resolvePath(artifactEntry.getStorageId(),
                                                                                  artifactEntry.getRepositoryId(),
                                                                                  artifactEntry.getArtifactPath());
            URL artifactResource = artifactResolutionService.resolveResource(repositoryPath);
            r.setUrl(artifactResource.toString());
        }

        return ResponseEntity.ok(result);
    }

}
