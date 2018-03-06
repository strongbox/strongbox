package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;

import io.swagger.annotations.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Alex Oreshkevich
 */
@Controller
@RequestMapping("/api/search")
@Api(value = "/api/search")
public class SearchController
        extends BaseController
{

    @Inject
    ArtifactSearchService artifactSearchService;

    /**
     * Performs a search against the Lucene index of a specified repository,
     * or the Lucene indexes of all repositories.
     *
     * @param storageId
     * @param repositoryId
     * @param query
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @ApiOperation(value = "Used to search for artifacts.", response = SearchResults.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "") })
    @PreAuthorize("hasAuthority('SEARCH_ARTIFACTS')")
    @RequestMapping(method = RequestMethod.GET,
                    consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                 MediaType.TEXT_PLAIN_VALUE },
                    produces = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE,
                                 MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity search(@ApiParam(value = "The storageId", required = false)
                                 @RequestParam(name = "storageId", required = false) final String storageId,
                                 @ApiParam(value = "The repositoryId", required = false)
                                 @RequestParam(name = "repositoryId", required = false) final String repositoryId,
                                 @ApiParam(value = "The search query", required = true)
                                 @RequestParam(name = "q") final String query,
                                 @ApiParam(value = "The search provider", required = false)
                                 @RequestParam(name = "searchProvider", required = false) final String searchProvider,
                                 HttpServletRequest request)
            throws IOException, SearchException
    {
        String accept = request.getHeader("accept");
        String q = URLDecoder.decode(query, "UTF-8");

        logger.debug("[search] " + q + "\n\taccept " + accept + "\n\tstorageId = " +
                     storageId + "\n\trepositoryId = " + repositoryId);

        if (accept.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE))
        {
            final SearchResults artifacts = getSearchResults(storageId, repositoryId, q, searchProvider);
            return ResponseEntity.ok(artifacts.toString());
        }
        else
        {
            // Apparently, the JSON root tag's name is based on the name of the object
            // which the Jersey method returns, hence this is "artifacts".
            @SuppressWarnings("UnnecessaryLocalVariable") final SearchResults artifacts = getSearchResults(storageId,
                                                                                                           repositoryId,
                                                                                                           q,
                                                                                                           searchProvider);
            return ResponseEntity.ok(artifacts);
        }
    }

    private SearchResults getSearchResults(String storageId,
                                           String repositoryId,
                                           String query,
                                           String searchProvider)
            throws SearchException
    {
        return artifactSearchService.search(new SearchRequest(storageId,
                                                              repositoryId,
                                                              query,
                                                              searchProvider));
    }

}
