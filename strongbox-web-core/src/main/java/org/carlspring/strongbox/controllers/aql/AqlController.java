package org.carlspring.strongbox.controllers.aql;

import java.io.IOException;

import javax.inject.Inject;

import org.carlspring.strongbox.aql.grammar.AqlQueryParser;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.AqlSearchService;
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

    @Inject
    private AqlSearchService aqlSearchService;

    @ApiOperation(value = "Used to search for artifacts.", response = SearchResults.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    @PreAuthorize("hasAuthority('SEARCH_ARTIFACTS')")
    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity search(@ApiParam(value = "Search query", required = true) @RequestParam(name = "query", required = true) String query)
        throws IOException,
        SearchException
    {
        AqlQueryParser parser = new AqlQueryParser(query);
        Selector<ArtifactEntry> selector = parser.parseQuery();

        SearchResults result = aqlSearchService.search(selector);

        return ResponseEntity.ok(result);
    }

}
