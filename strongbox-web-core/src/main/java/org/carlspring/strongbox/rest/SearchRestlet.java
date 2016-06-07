package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@Path("/search")
@Api(value = "/search")
@PreAuthorize("hasAuthority('ROOT')")
public class SearchRestlet
        extends BaseArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);

    @Autowired
    private ArtifactSearchService artifactSearchService;

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
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @ApiOperation(value = "Used to search for artifacts.", response = SearchResults.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "") })
    public Response search(@ApiParam(value = "The storageId", required = true)
                                @QueryParam("storageId") final String storageId,
                                @ApiParam(value = "The repositoryId", required = true)
                                @QueryParam("repositoryId") final String repositoryId,
                                @ApiParam(value = "The search query", required = true)
                                @QueryParam("q") final String query,
                                @Context HttpHeaders headers,
                                @Context HttpServletRequest request)
            throws IOException, ParseException
    {
        if (request.getHeader("accept").toLowerCase().equals("text/plain"))
        {
            final SearchResults artifacts = getSearchResults(storageId, repositoryId, query);

            return Response.ok(artifacts.toString()).build();
        }
        else
        {
            // Apparently, the JSON root tag's name is based on the name of the object
            // which the Jersey method returns, hence this is "artifacts".
            @SuppressWarnings("UnnecessaryLocalVariable")
            final SearchResults artifacts = getSearchResults(storageId, repositoryId, query);

            return Response.ok(artifacts).build();
        }
    }

    private SearchResults getSearchResults(String storageId,
                                           String repositoryId,
                                           String query)
            throws IOException, ParseException
    {
        final SearchRequest searchRequest = new SearchRequest(storageId, repositoryId, query);

        return artifactSearchService.search(searchRequest);
    }

}
