package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.security.authorization.AuthorizationException;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    public Response upload(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path,
                           @Context HttpHeaders headers,
                           @Context HttpServletRequest request,
                           InputStream is)
            throws IOException,
                   AuthenticationException,
                   NoSuchAlgorithmException
    {
        String protocol = request.getRequestURL().toString().split(":")[0];

        if (requiresAuthentication(storage, repository, path, protocol))
        {
            if (validateAuthentication(storage, repository, path, headers, protocol))
            {

            }
            else
            {
                // Return HTTP 401
                throw new AuthorizationException("You are not authorized to deploy artifacts to this repository.");
            }
        }

        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is, new String[]{ "MD5", "SHA-1" });

        int size = 4096;
        byte[] bytes = new byte[size];

        while (mdis.read(bytes, 0, size) != -1)
        {
            // TODO: Store the file
        }

        MessageDigest md5Digest = mdis.getMessageDigest("MD5");
        MessageDigest sha1Digest = mdis.getMessageDigest("SHA-1");

        String md5 = MessageDigestUtils.convertToHexadecimalString(md5Digest);
        String sha1 = MessageDigestUtils.convertToHexadecimalString(sha1Digest);

        System.out.println("md5:  " + md5);
        System.out.println("sha1: " + sha1);


        // TODO: Do something with the artifact
        // Repository r = getDataCenter().getStorage(storage).getRepository(repository);
        // TODO: If the repository's type is In-Memory, do nothing.
        // TODO: For all other type of repositories, invoke the respective storage provider.

        return Response.ok().build();
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
