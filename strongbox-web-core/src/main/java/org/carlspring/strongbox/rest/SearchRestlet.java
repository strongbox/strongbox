package org.carlspring.strongbox.rest;

import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.index.ArtifactInfo;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.Set;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @GET
    @Path("{repository}")
    public String upload(@PathParam("repository") final String repository,
                         @QueryParam("q") final String queryText)
            throws IOException, ParseException
    {
        final Set<ArtifactInfo> results = repositoryIndexManager.getRepositoryIndex(repository).search(queryText);
        final StringBuilder response = new StringBuilder();
        for (final ArtifactInfo result : results)
        {
            response.append(result.toString()).append(System.lineSeparator());
        }
        return response.toString();
    }
}