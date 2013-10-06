package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Todorov
 */
@Component
@Path("/storages")
public class ArtifactRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRestlet.class);


    @PUT
    @Path("{storage}/{repository}/{path:.*}")
    public void upload(@PathParam("storage") String storage,
                       @PathParam("repository") String repository,
                       @PathParam("path") String path,
                       @Context HttpHeaders headers,
                       byte[] in)
            throws IOException
    {
        // TODO: Implement

        // final List<String> authorizationHeaders = headers.getRequestHeader("authorization");
        // validateAuthentication(storage, )


        /*
        String path = request.getPath(true);
        String method = request.getMethod();
        String authorizationHeader = request.getHeaderValue("authorization");
        */

        logger.debug("/storages/" + storage + "/" + repository + "/" + path + " requires authentication?  " +
                     requiresAuthentication(storage, repository, path));



        System.out.println("PUT: " + path);
        logger.debug("PUT: " + path);

        System.out.println("getDataCenter(): " + getDataCenter());
        System.out.println("getDataCenter.getStorages.size(): " + getDataCenter().getStorages().size());
    }

    @GET
    @Path("{storage}/{repository}/{path:.*}")
    public Response download(@PathParam("storage") String storage,
                             @PathParam("repository") String repository,
                             @PathParam("path") String path)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException
    {
        logger.debug(" repository = " + repository + ", path = " + path);

        if (!ArtifactUtils.isArtifact(path))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        InputStream is;
        try
        {
            is = ArtifactResolutionService.getInstance().getInputStream(repository, path);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Response.ok(is).build();
    }

    @DELETE
    @Path("{storage}/{repository}/{path:.*}")
    public void delete(@PathParam("storage") String storage,
                       @PathParam("repository") String repository,
                       @PathParam("path") String path)
            throws IOException
    {
        // TODO: Implement

        System.out.println("DELETE: " + path);
        logger.debug("DELETE: " + path);
    }

}
