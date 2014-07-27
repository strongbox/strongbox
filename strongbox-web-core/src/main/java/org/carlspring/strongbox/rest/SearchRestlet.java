package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.rest.serialization.search.ArtifactSearchJSONSerializer;
import org.carlspring.strongbox.rest.serialization.search.ArtifactSearchPlainTextSerializer;
import org.carlspring.strongbox.rest.serialization.search.ArtifactSearchSerializer;
import org.carlspring.strongbox.rest.serialization.search.ArtifactSearchXMLSerializer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.storage.services.ArtifactSearchService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;

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

    public static final String OUTPUT_FORMAT_PLAIN_TEXT = "text";

    public static final String OUTPUT_FORMAT_XML = "xml";

    public static final String OUTPUT_FORMAT_JSON = "json";


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
    public Response search(@QueryParam("storage") final String storage,
                           @QueryParam("repository") final String repository,
                           @QueryParam("q") final String query,
                           @DefaultValue(OUTPUT_FORMAT_PLAIN_TEXT) @QueryParam("format") final String format,
                           @DefaultValue("false") @QueryParam("indent") final String indent)
            throws IOException, ParseException
    {
        final SearchRequest searchRequest = new SearchRequest(storage, repository, query);
        final SearchResults searchResults = artifactSearchService.search(searchRequest);

        final boolean useIndentation = Boolean.parseBoolean(indent);

        return Response.ok(new StreamingOutput()
        {
            @Override
            public void write(final OutputStream os)
                    throws IOException, WebApplicationException
            {
                try
                {
                    ArtifactSearchSerializer serializer;
                    switch (format)
                    {
                        case OUTPUT_FORMAT_XML:
                            serializer = new ArtifactSearchXMLSerializer(configurationManager);
                            break;
                        case OUTPUT_FORMAT_JSON:
                            serializer = new ArtifactSearchJSONSerializer(configurationManager);
                            break;
                        default:
                            serializer = new ArtifactSearchPlainTextSerializer(configurationManager);
                            break;
                    }

                    serializer.write(searchResults, os, useIndentation);
                }
                catch (XMLStreamException e)
                {
                    throw new IOException(e.getMessage(), e);
                }
            }

        }).type(OUTPUT_FORMAT_XML.equals(format) ? MediaType.APPLICATION_XML  :
                OUTPUT_FORMAT_JSON.equals(format) ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN).build();
    }

}
