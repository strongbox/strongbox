package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                       @Context HttpServletRequest request,
                       byte[] in)
            throws IOException,
                   AuthenticationException
    {
        String protocol = request.getRequestURL().toString().split(":")[0];

        if (requiresAuthentication(storage, repository, path, protocol))
        {
            validateAuthentication(storage, repository, path, headers, protocol);
        }

        System.out.println("getDataCenter(): " + getDataCenter());
        System.out.println("getDataCenter.getStorages.size(): " + getDataCenter().getStorages().size());

        // TODO: Do something with the artifact
        // Repository r = getDataCenter().getStorage(storage).getRepository(repository);
        // TODO: If the repository's type is In-Memory, do nothing.
        // TODO: For all other type of repositories, invoke the respective storage provider.
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
