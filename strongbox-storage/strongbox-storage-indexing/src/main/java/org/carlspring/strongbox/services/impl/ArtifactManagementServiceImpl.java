package org.carlspring.strongbox.services.impl;

import org.apache.lucene.store.FSDirectory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.security.encryption.EncryptionConstants;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.carlspring.strongbox.util.RepositoryUtils.checkRepositoryExists;

/**
 * @author mtodorov
 */
@Component
public class ArtifactManagementServiceImpl
    implements ArtifactManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementServiceImpl.class);

    @Autowired
    private ArtifactResolutionService artifactResolutionService;

    @Autowired
    private VersionValidatorService versionValidatorService;

    @Autowired
    private ChecksumCacheManager checksumCacheManager;

    @Autowired
    private DataCenter dataCenter;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @Override
    public void store(String storage,
                      String repositoryId,
                      String path,
                      InputStream is)
            throws ArtifactStorageException
    {
        performRepositoryAcceptanceValidation(repositoryId, path);

        boolean fileIsChecksum = path.endsWith(".md5") || path.endsWith(".sha1");
        MultipleDigestInputStream mdis = null;
        try
        {
            mdis = new MultipleDigestInputStream(is);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ArtifactStorageException();
        }

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
            os = artifactResolutionService.getOutputStream(repositoryId, path);

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

            final String artifactPath = storage + "/" + repositoryId + "/" + path;
            if (!fileIsChecksum && os != null)
            {
                addChecksumsToCacheManager(mdis, artifactPath);

                if (ArtifactFileUtils.isArtifactFile(path))
                {
                    final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storage + ":" + repositoryId);
                    if (indexer != null)
                    {
                        final Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                        final File storageBasedir = new File(dataCenter.getStorage(storage).getBasedir());
                        final File artifactFile = new File(new File(storageBasedir, repositoryId), path).getCanonicalFile();

                        if (!artifactFile.getName().endsWith(".pom"))
                        {
                            indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
                        }
                    }
                }
            }
            else
            {
                validateUploadedChecksumAgainstCache(baos, artifactPath);
            }
        }
        catch (ArtifactResolutionException e)
        {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }
    }

    @Override
    public InputStream resolve(String storage,
                               String repository,
                               String path)
            throws ArtifactResolutionException
    {
        InputStream is = null;

        try
        {
            is = artifactResolutionService.getInputStream(repository, path);
            return is;
        }
        catch (IOException e)
        {
            throw new ArtifactResolutionException(e.getMessage(), e);
        }
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

    @Override
    public void delete(String storage,
                       String repositoryName,
                       String artifactPath)
            throws ArtifactStorageException
    {
        try
        {
            final Repository repository = dataCenter.getStorage(storage).getRepository(repositoryName);
            checkRepositoryExists(repositoryName, repository);

            LocationResolver resolver = getResolvers().get(repository.getImplementation());

            resolver.delete(repositoryName, artifactPath);

            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storage + ":" + repositoryName);
            if (indexer != null)
            {
                String extension = artifactPath.substring(artifactPath.lastIndexOf(".") + 1, artifactPath.length());

                final Artifact a = ArtifactUtils.convertPathToArtifact(artifactPath);
                indexer.delete(Arrays.asList(new ArtifactInfo(repositoryName,
                                                              a.getGroupId(),
                                                              a.getArtifactId(),
                                                              a.getVersion(),
                                                              a.getClassifier(),
                                                              extension)));
            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
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

    private Map<String, LocationResolver> getResolvers()
    {
        return artifactResolutionService.getResolvers();
    }

    // TODO: This should have restricted access.
    @Override
    public void deleteTrash(String storage, String repositoryName)
            throws ArtifactStorageException
    {
        try
        {
            final Repository repository = dataCenter.getStorage(storage).getRepository(repositoryName);
            checkRepositoryExists(repositoryName, repository);

            LocationResolver resolver = getResolvers().get(repository.getImplementation());
            resolver.deleteTrash(repositoryName);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    // TODO: This should have restricted access.
    @Override
    public void deleteTrash()
            throws ArtifactStorageException
    {
        try
        {
            for (LocationResolver resolver : getResolvers().values())
            {
                resolver.deleteTrash();
            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void merge(String sourceStorage,
                      String sourceRepositoryName,
                      String targetStorage,
                      String targetRepositoryName)
            throws ArtifactStorageException
    {
        try
        {
            final RepositoryIndexer sourceIndex = repositoryIndexManager
                    .getRepositoryIndex(sourceStorage + ":" + sourceRepositoryName);
            if (sourceIndex == null) throw new ArtifactStorageException("source repo not found");

            final RepositoryIndexer targetIndex = repositoryIndexManager
                    .getRepositoryIndex(targetStorage + ":" + targetRepositoryName);
            if (targetIndex == null) throw new ArtifactStorageException("target repo not found");

            targetIndex.getIndexingContext().merge(FSDirectory.open(sourceIndex.getIndexDir()));
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }
}
