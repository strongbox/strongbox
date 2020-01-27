package org.carlspring.strongbox.controllers.aql;

import org.carlspring.strongbox.aql.grammar.AqlQueryParser;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.services.AqlSearchService;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.inject.Inject;
import java.io.IOException;

import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity search(@ApiParam(value = "Search query", required = true) @RequestParam(name = "query", required = true) String query)
        throws IOException
    {
        AqlQueryParser parser = new AqlQueryParser(query);
        Selector<ArtifactEntity> selector = parser.parseQuery();

        SearchResults result = aqlSearchService.search(selector);

        return ResponseEntity.ok(result);
    }

}
