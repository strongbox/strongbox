package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);


    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private ConfigurationManager configurationManager;


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
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SearchResults search(@QueryParam("storageId") final String storageId,
                                @QueryParam("repositoryId") final String repositoryId,
                                @QueryParam("q") final String query)
            throws IOException, ParseException
    {
        // Apparently, the JSON root tag's name is based on the name of the object
        // which the Jersey method returns, hence this is "artifacts".
        @SuppressWarnings("UnnecessaryLocalVariable")
        final SearchResults artifacts = getSearchResults(storageId, repositoryId, query);

        return artifacts;
    }

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
    @Produces({ MediaType.TEXT_PLAIN })
    public String searchAsPlainText(@QueryParam("storage") final String storageId,
                                    @QueryParam("repository") final String repositoryId,
                                    @QueryParam("q") final String query)
            throws IOException, ParseException
    {
        final SearchResults artifacts = getSearchResults(storageId, repositoryId, query);

        return artifacts.toString();
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
