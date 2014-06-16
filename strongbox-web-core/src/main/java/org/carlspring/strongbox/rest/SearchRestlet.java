package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);

    @Autowired
    private ArtifactResolutionService artifactResolutionService;

    @Autowired
    private DataCenter dataCenter;


    @PUT
    @Path("{storage}/{repository}")
    public Response upload(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @QueryParam("q") String path)
    {

        return Response.ok().build();
    }
}
