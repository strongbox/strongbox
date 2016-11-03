package org.carlspring.strongbox.controller;

import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URLDecoder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Alex Oreshkevich
 */
@Controller
@RequestMapping("/search")
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
    @RequestMapping(value = "",
                    method = RequestMethod.GET,
                    consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                 MediaType.TEXT_PLAIN_VALUE },
                    produces = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE,
                                 MediaType.TEXT_PLAIN_VALUE }
    )
    public ResponseEntity search(@RequestParam(name = "storageId", required = false) final String storageId,
                                 @RequestParam(name = "repositoryId") final String repositoryId,
                                 @RequestParam(name = "q") final String query,
                                 @RequestHeader HttpHeaders headers,
                                 HttpServletRequest request)
            throws IOException, ParseException, JAXBException
    {
        if (request.getHeader("accept").equalsIgnoreCase("text/plain"))
        {
            final SearchResults artifacts = getSearchResults(storageId, repositoryId, URLDecoder.decode(query, "UTF-8"));
            return ResponseEntity.ok(artifacts.toString());
        }
        else
        {
            // Apparently, the JSON root tag's name is based on the name of the object
            // which the Jersey method returns, hence this is "artifacts".
            @SuppressWarnings("UnnecessaryLocalVariable")
            final SearchResults artifacts = getSearchResults(storageId, repositoryId, query);
            return ResponseEntity.ok(artifacts);
        }
    }

    private SearchResults getSearchResults(String storageId,
                                           String repositoryId,
                                           String query)
            throws IOException, ParseException
    {
        final SearchRequest searchRequest = new SearchRequest(storageId, repositoryId, URLDecoder.decode(query, "UTF-8"));

        return artifactSearchService.search(searchRequest);
    }

}
