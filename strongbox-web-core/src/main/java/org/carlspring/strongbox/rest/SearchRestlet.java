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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{


    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private ConfigurationManager configurationManager;


    /**
     * Performs a search against the Lucene index of a specified repository,
     * or the Lucene indexes of all repositories.
     *
     * @param repository
     * @param query
     * @param format
     * @param indent
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public SearchResults search(@QueryParam("storage") final String storage,
                           @QueryParam("repository") final String repository,
                           @QueryParam("q") final String query/*,
                           @DefaultValue("false") @QueryParam("indent") final String indent*/)
            throws IOException, ParseException
    {
        final SearchRequest searchRequest = new SearchRequest(storage, repository, query);
        final SearchResults searchResults = artifactSearchService.search(searchRequest);

        return searchResults;
    }

}
