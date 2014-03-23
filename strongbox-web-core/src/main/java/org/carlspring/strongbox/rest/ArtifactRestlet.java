package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.security.encryption.EncryptionConstants;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    @Autowired
    private ChecksumCacheManager checksumCacheManager;

    @Autowired
    private ArtifactResolutionService artifactResolutionService;


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

        handleAuthentication(storage, repository, path, headers, protocol);

        boolean fileIsChecksum = path.endsWith(".md5") || path.endsWith(".sha1");
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is, new String[]{ EncryptionConstants.ALGORITHM_MD5,
                                                                                         EncryptionConstants.ALGORITHM_SHA1 });

        // TODO: Do something with the artifact
        // Repository r = getDataCenter().getStorage(storage).getRepository(repository);
        // TODO: If the repository's type is In-Memory, do nothing.
        // TODO: For all other type of repositories, invoke the respective storage provider.

        // TODO: If this is not a checksum file, store the file.
        // TODO: If this is a checksum file, keep the hash in a String.
        ByteArrayOutputStream baos = null;
        if (fileIsChecksum)
        {
            baos = new ByteArrayOutputStream();
        }

        OutputStream os = null;
        try
        {
            os = artifactResolutionService.getOutputStream(repository, path);

            int readLength;
            byte[] bytes = new byte[4096];

            while ((readLength = mdis.read(bytes, 0, bytes.length)) != -1)
            {
                /*
                if (fileIsChecksum)
                {
                    // Buffer the checksum for later validation
                    baos.write(bytes, 0, readLength);
                    baos.flush();
                }
                else
                {
                */
                    os.write(bytes, 0, readLength);
                    os.flush();
                    // Write the artifact
                //}
            }

            System.out.println("# Wrote " + readLength + " bytes.");
        }
        catch (ArtifactResolutionException e)
        {
            e.printStackTrace();
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }

        final String artifactPath = storage + "/" + repository + "/" + path;
        if (!fileIsChecksum)
        {
            addChecksumsToCacheManager(mdis, artifactPath);
        }
        else
        {
            validateUploadedChecksumAgainstCache(baos, artifactPath);
        }

        return Response.ok().build();
    }

    private void validateUploadedChecksumAgainstCache(ByteArrayOutputStream baos,
                                                      String artifactPath)
    {
        logger.debug("Received checksum: " + baos.toString());

        String artifactBasePath = artifactPath.substring(0, artifactPath.lastIndexOf('.'));
        String algorithm = null;

        final String checksumExtension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1, artifactPath.length());
        if (checksumExtension.equalsIgnoreCase(EncryptionConstants.ALGORITHM_MD5))
        {
            algorithm = EncryptionConstants.ALGORITHM_MD5;
        }
        else if (checksumExtension.equals("sha1"))
        {
            algorithm = EncryptionConstants.ALGORITHM_SHA1;
        }
        else
        {
            // TODO: Should we be doing something about this case?
            logger.warn("Unsupported checksum type: " + checksumExtension);
        }

        final boolean matchesCachedChecksum = matchesChecksum(baos, artifactBasePath, algorithm);
        if (matchesCachedChecksum)
        {
            checksumCacheManager.removeArtifactChecksum(artifactBasePath, algorithm);
        }
        else
        {
            // TODO: Implement event triggering that handles checksums that don't match the upload file.
        }
    }

    private boolean matchesChecksum(ByteArrayOutputStream baos,
                                    String artifactBasePath,
                                    String algorithm)
    {
        String checksum = baos.toString();
        String cachedChecksum = checksumCacheManager.getArtifactChecksum(artifactBasePath, algorithm);

        if (cachedChecksum.equals(checksum))
        {
            logger.debug("The received " + algorithm + " checksum matches cached one! " + checksum);
            return true;
        }
        else
        {
            logger.debug("The received " + algorithm + " does not match cached one! " + checksum + "/" + cachedChecksum);
            return false;
        }
    }

    private void addChecksumsToCacheManager(MultipleDigestInputStream mdis,
                                            String artifactPath)
    {
        MessageDigest md5Digest = mdis.getMessageDigest(EncryptionConstants.ALGORITHM_MD5);
        MessageDigest sha1Digest = mdis.getMessageDigest(EncryptionConstants.ALGORITHM_SHA1);

        String md5 = MessageDigestUtils.convertToHexadecimalString(md5Digest);
        String sha1 = MessageDigestUtils.convertToHexadecimalString(sha1Digest);

        checksumCacheManager.addArtifactChecksum(artifactPath, EncryptionConstants.ALGORITHM_MD5, md5);
        checksumCacheManager.addArtifactChecksum(artifactPath, EncryptionConstants.ALGORITHM_SHA1, sha1);
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
            is = artifactResolutionService.getInputStream(repository, path);
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
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

        logger.debug("DELETE: " + path);
    }

    public ChecksumCacheManager getChecksumCacheManager()
    {
        return checksumCacheManager;
    }

    public void setChecksumCacheManager(ChecksumCacheManager checksumCacheManager)
    {
        this.checksumCacheManager = checksumCacheManager;
    }

}
