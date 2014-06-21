package org.carlspring.strongbox.rest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.security.encryption.EncryptionConstants;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.carlspring.strongbox.storage.validation.version.VersionValidatorService;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Set;

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

    @Autowired
    private VersionValidatorService versionValidatorService;

    @Autowired
    private DataCenter dataCenter;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


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

        performRepositoryAcceptanceValidation(repository, path);

        boolean fileIsChecksum = path.endsWith(".md5") || path.endsWith(".sha1");
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is,
                                                                       new String[]{ EncryptionConstants.ALGORITHM_MD5,
                                                                                     EncryptionConstants.ALGORITHM_SHA1 });

        // If this is not a checksum file, store the file.
        // If this is a checksum file, keep the hash in a String.
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
                if (fileIsChecksum)
                {
                    // Buffer the checksum for later validation
                    baos.write(bytes, 0, readLength);
                    baos.flush();
                }
                else
                {
                    os.write(bytes, 0, readLength);
                    os.flush();
                    // Write the artifact
                }
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
        if (!fileIsChecksum && os != null)
        {
            addChecksumsToCacheManager(mdis, artifactPath);
            logger.info("pre-check; repo: {}; path: {}", repository, artifactPath);
            if (!path.contains("/maven-metadata."))
            {
                final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(repository);
                if (indexer != null)
                {
                    final Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                    final File storageBasedir = new File(dataCenter.getStorage(storage).getBasedir());
                    final File artifactFile = new File(storageBasedir, artifactPath).getCanonicalFile();

                    logger.info("pre-indexer; repo: {}; file: {}", repository, artifactFile.toString());
                    indexer.addArtifactToIndex(repository, artifactFile, artifact);
                }
                else
                {
                    logger.info("no indexer for repo: {}", repository);
                }
            }
        }
        else
        {
            validateUploadedChecksumAgainstCache(baos, artifactPath);
        }

        return Response.ok().build();
    }

    private boolean performRepositoryAcceptanceValidation(String repository,
                                                          String path)
            throws WebApplicationException
    {
        if (!path.contains("/maven-metadata."))
        {
            try
            {
                Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                Repository r = dataCenter.getRepository(repository);

                final Set<VersionValidator> validators = versionValidatorService.getValidators();
                for (VersionValidator validator : validators)
                {
                    validator.validate(r, artifact);
                }
            }
            catch (VersionValidationException e)
            {
                throw new WebApplicationException(e, Response.Status.FORBIDDEN);
            }
        }

        return true;
    }

    private void validateUploadedChecksumAgainstCache(ByteArrayOutputStream baos,
                                                      String artifactPath)
    {
        logger.debug("Received checksum: " + baos.toString());

        String artifactBasePath = artifactPath.substring(0, artifactPath.lastIndexOf('.'));
        String algorithm = null;

        final String checksumExtension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1,
                                                                artifactPath.length());
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
            logger.debug("The received " + algorithm + " does not match cached one! " + checksum + "/" +
                         cachedChecksum);
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
                             @PathParam("path") String path,
                             @Context HttpServletRequest request,
                             @Context HttpHeaders headers)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException,
                   AuthenticationException
    {
        logger.debug(" repository = " + repository + ", path = " + path);

        String protocol = request.getRequestURL().toString().split(":")[0];
        handleAuthentication(storage, repository, path, headers, protocol);

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
    public Response delete(@PathParam("storage") String storage,
                           @PathParam("repository") String repository,
                           @PathParam("path") String path)
            throws IOException
    {
        logger.debug("DELETE: " + path);
        logger.debug(" repository = " + repository + ", path = " + path);

        try
        {
            artifactResolutionService.delete(repository, path);

            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(repository);
            if (indexer != null)
            {
                final Artifact a = ArtifactUtils.convertPathToArtifact(path);
                indexer.delete(Arrays.asList(new ArtifactInfo(repository,
                                                              a.getGroupId(),
                                                              a.getArtifactId(),
                                                              a.getVersion(),
                                                              a.getClassifier())));
            }
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }

        return Response.ok().build();
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
