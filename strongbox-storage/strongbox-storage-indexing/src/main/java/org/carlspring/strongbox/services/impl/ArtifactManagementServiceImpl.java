package org.carlspring.strongbox.services.impl;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.security.encryption.EncryptionConstants;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
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
    private ConfigurationManager configurationManager;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @Override
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is)
            throws ArtifactStorageException
    {
        performRepositoryAcceptanceValidation(storageId, repositoryId, path);

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
            os = artifactResolutionService.getOutputStream(storageId, repositoryId, path);

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

                // Write the artifact
                os.write(bytes, 0, readLength);
                os.flush();
            }

            final String artifactPath = storageId + "/" + repositoryId + "/" + path;
            if (!fileIsChecksum && os != null)
            {
                addChecksumsToCacheManager(mdis, artifactPath);

                if (ArtifactFileUtils.isArtifactFile(path))
                {
                    final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);
                    if (indexer != null)
                    {
                        final Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                        final Storage storage = getStorage(storageId);
                        final File storageBasedir = new File(storage.getBasedir());
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
            throw new ArtifactStorageException(e);
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
    public InputStream resolve(String storageId,
                               String repositoryId,
                               String path)
            throws ArtifactResolutionException
    {
        InputStream is = null;

        try
        {
            is = artifactResolutionService.getInputStream(storageId, repositoryId, path);
            return is;
        }
        catch (IOException e)
        {
            throw new ArtifactResolutionException(e.getMessage(), e);
        }
    }

    private boolean performRepositoryAcceptanceValidation(String storageId,
                                                          String repositoryId,
                                                          String path)
            throws ArtifactStorageException
    {
        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        if (!path.contains("/maven-metadata."))
        {
            try
            {
                Artifact artifact = ArtifactUtils.convertPathToArtifact(path);

                final Set<VersionValidator> validators = versionValidatorService.getVersionValidators();
                for (VersionValidator validator : validators)
                {
                    validator.validate(repository, artifact);
                }
            }
            catch (VersionValidationException e)
            {
                throw new ArtifactStorageException(e);
            }
        }

        checkAllowsDeployment(repository);
        checkAllowsRedeployment(repository, ArtifactUtils.convertPathToArtifact(path));

        return true;
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String artifactPath,
                       boolean force)
            throws ArtifactStorageException
    {
        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        checkAllowsDeletion(repository);

        try
        {
            checkRepositoryExists(repositoryId, repository);

            LocationResolver resolver = getResolvers().get(repository.getImplementation());

            resolver.delete(storageId, repositoryId, artifactPath, force);

            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);
            if (indexer != null)
            {
                String extension = artifactPath.substring(artifactPath.lastIndexOf(".") + 1, artifactPath.length());

                final Artifact a = ArtifactUtils.convertPathToArtifact(artifactPath);
                indexer.delete(Collections.singletonList(new ArtifactInfo(repositoryId,
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

    private void checkAllowsDeployment(Repository repository)
            throws ArtifactStorageException
    {
        if (!repository.allowsDeployment())
        {
            throw new ArtifactStorageException("Deployment of artifacts to " + repository.getType() + " repository is not allowed!");
        }
    }

    private void checkAllowsRedeployment(Repository repository, Artifact artifact)
            throws ArtifactStorageException
    {
        if (repository.containsArtifact(artifact) && !repository.allowsDeployment())
        {
            throw new ArtifactStorageException("Re-deployment of artifacts to " + repository.getType() + " repository is not allowed!");
        }
    }

    private void checkAllowsDeletion(Repository repository)
            throws ArtifactStorageException
    {
        if (!repository.allowsDeletion())
        {
            throw new ArtifactStorageException("Deleting artifacts from " + repository.getType() + " repository is not allowed!");
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
    public void deleteTrash(String storageId, String repositoryId)
            throws ArtifactStorageException
    {
        try
        {
            final Storage storage = getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);

            checkRepositoryExists(repositoryId, repository);
            checkAllowsDeletion(repository);

            LocationResolver resolver = getResolvers().get(repository.getImplementation());
            resolver.deleteTrash(storageId, repositoryId);
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

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    private Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

}
