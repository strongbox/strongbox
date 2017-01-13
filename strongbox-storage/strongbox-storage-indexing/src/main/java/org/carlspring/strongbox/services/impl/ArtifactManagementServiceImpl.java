package org.carlspring.strongbox.services.impl;

import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.ArtifactChecksum;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("artifactManagementService")
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

    @Autowired
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;


    @Override
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is)
            throws IOException, ProviderImplementationException, NoSuchAlgorithmException
    {
        String artifactPath = storageId + "/" + repositoryId + "/" + path;
        performRepositoryAcceptanceValidation(storageId, repositoryId, path);
        
        
        try(ArtifactOutputStream os = artifactResolutionService.getOutputStream(storageId, repositoryId, path))
        {
            //If we have no Digests then we have a Checksum to store.
            if (os.getDigests().isEmpty())
            {
                os.setCacheOutputStream(new ByteArrayOutputStream());
            }

            int readLength;
            byte[] bytes = new byte[4096];
            while ((readLength = is.read(bytes, 0, bytes.length)) != -1)
            {
                // Write the artifact
                os.write(bytes, 0, readLength);
                os.flush();
            }

            byte[] checksum = os.getCacheOutputStream() != null ? ((ByteArrayOutputStream) os.getCacheOutputStream()).toByteArray() : null;
            if (checksum == null)
            {
                addChecksumsToCacheManager(os.getDigestMap(), artifactPath);
                addArtifactToIndex(storageId, repositoryId, path);
            }
            else
            {
                
                validateUploadedChecksumAgainstCache(checksum,
                                                     artifactPath);

            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e);
        }
    }

    private void addArtifactToIndex(String storageId, String repositoryId, String path)
            throws IOException
    {
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

    @Override
    public InputStream resolve(String storageId,
                               String repositoryId,
                               String path)
            throws IOException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        InputStream is;

        try
        {
            is = artifactResolutionService.getInputStream(storageId, repositoryId, path);
            return is;
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            // This is not necessarily an error. It could simply be a check
            // whether a resource exists, before uploading/updating it.
            logger.info("The requested path does not exist: /" + storageId + "/" + repositoryId + "/" + path);
        }

        return null;
    }

    private boolean performRepositoryAcceptanceValidation(String storageId,
                                                          String repositoryId,
                                                          String path)
            throws IOException, ProviderImplementationException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, path);

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        if (!path.contains("/maven-metadata.") &&
            !ArtifactUtils.isMetadata(path) && !ArtifactUtils.isChecksum(path))
        {
            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            ArtifactCoordinates coordinates = (ArtifactCoordinates) layoutProvider.getArtifactCoordinates(path);

            try
            {
                final Set<VersionValidator> validators = versionValidatorService.getVersionValidators();
                for (VersionValidator validator : validators)
                {
                    validator.validate(repository, coordinates);
                }
            }
            catch (VersionValidationException e)
            {
                throw new ArtifactStorageException(e);
            }

            artifactOperationsValidator.checkAllowsRedeployment(repository, coordinates);
            artifactOperationsValidator.checkAllowsDeployment(repository);
        }

        return true;
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String artifactPath,
                       boolean force)
            throws IOException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        artifactOperationsValidator.checkAllowsDeletion(repository);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
            layoutProvider.delete(storageId, repositoryId, artifactPath, force);

            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);
            if (indexer != null)
            {
                String extension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1, artifactPath.length());

                final Artifact a = ArtifactUtils.convertPathToArtifact(artifactPath);

                /* TODO: This needs to be properly fixed:
                indexer.delete(Collections.singletonList(new ArtifactInfo(repositoryId,
                                                                          a.getGroupId(),
                                                                          a.getArtifactId(),
                                                                          a.getVersion(),
                                                                          a.getClassifier(),
                                                                          extension)));
                */
            }
        }
        catch (IOException | ProviderImplementationException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public boolean contains(String storageId, String repositoryId, String artifactPath)
            throws IOException
    {
        return false;
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String path,
                     String destStorageId,
                     String destRepositoryId)
            throws IOException
    {
        artifactOperationsValidator.validate(srcStorageId, srcRepositoryId, path);

        final Storage srcStorage = getStorage(srcStorageId);
        final Repository srcRepository = srcStorage.getRepository(srcRepositoryId);

        final Storage destStorage = getStorage(destStorageId);
        final Repository destRepository = destStorage.getRepository(destRepositoryId);

        File srcFile = new File(srcRepository.getBasedir(), path);
        File destFile = new File(destRepository.getBasedir(), path);

        if (srcFile.isDirectory())
        {
            FileUtils.copyDirectoryToDirectory(srcFile, destFile.getParentFile());

            // TODO: SB-377: Sort out the logic for artifact directory paths
            // TODO: SB-377: addArtifactToIndex(destStorageId, destRepositoryId, path);
        }
        else
        {
            FileUtils.copyFile(srcFile, destFile);
            addArtifactToIndex(destStorageId, destRepositoryId, path);
        }
    }

    private void validateUploadedChecksumAgainstCache(byte[] checksum,
                                                      String artifactPath)
    {
        logger.debug("Received checksum: " + new String(checksum));

        String artifactBasePath = artifactPath.substring(0, artifactPath.lastIndexOf('.'));
        String algorithm = null;

        final String checksumExtension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1,
                                                                artifactPath.length());
        if (!matchesChecksum(checksum, artifactBasePath, checksumExtension))
        {
            // TODO: Implement event triggering that handles checksums that don't match the uploaded file.
        }
        checksumCacheManager.removeArtifactChecksum(artifactBasePath, algorithm);
    }

    private boolean matchesChecksum(byte[] pChecksum,
                                    String artifactBasePath,
                                    String checksumExtension)
    {
        String checksum = new String(pChecksum);
        ArtifactChecksum artifactChecksum = checksumCacheManager.getArtifactChecksum(artifactBasePath);

        Map<Boolean, Set<String>> matchingMap = artifactChecksum.getChecksums()
                                                                .entrySet()
                                                                .stream()
                                                                .collect(Collectors.groupingBy(e -> e.getValue()
                                                                                                     .equals(checksum),
                                                                                               Collectors.mapping(e -> e.getKey(),
                                                                                                                  Collectors.toSet())));

        Set<String> matched = matchingMap.get(Boolean.TRUE);
        Set<String> unmatched = matchingMap.get(Boolean.FALSE);
        logger.debug(String.format("Artifact checksum matchings: artifact-[%s]; ext-[%s]; matched-[%s]; unmatched-[%s]; checksum-[%s]",
                                   artifactBasePath, checksumExtension, matched, unmatched,
                                   new String(checksum)));

        return matched == null || matched.isEmpty();
    }

    private void addChecksumsToCacheManager(Map<String, String> digestMap,
                                            String artifactPath)
    {
        digestMap.entrySet()
                 .stream()
                 .forEach(e -> checksumCacheManager.addArtifactChecksum(artifactPath, e.getKey(), e.getValue()));
    }

    // TODO: This should have restricted access.
    @Override
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final Storage storage = getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);

            artifactOperationsValidator.checkAllowsDeletion(repository);

            LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
            layoutProvider.deleteTrash(storageId, repositoryId);
        }
        catch (IOException | ProviderImplementationException e)
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
            layoutProviderRegistry.deleteTrash();
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undelete(String storageId, String repositoryId, String artifactPath)
            throws IOException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        artifactOperationsValidator.checkAllowsDeletion(repository);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
            layoutProvider.undelete(storageId, repositoryId, artifactPath);

            /*
            // TODO: This will need further fixing:
            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(storageId + ":" + repositoryId);
            if (indexer != null)
            {
                final Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);
                final File artifactFile = new File(repository.getBasedir(), artifactPath);

                indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
            }
            */
        }
        catch (IOException | ProviderImplementationException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash(String storageId, String repositoryId)
            throws IOException,
                   ProviderImplementationException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final Storage storage = getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);

            if (repository.isTrashEnabled())
            {
                LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
                layoutProvider.undeleteTrash(storageId, repositoryId);
            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash()
            throws IOException,
                   ProviderImplementationException
    {
        try
        {
            layoutProviderRegistry.undeleteTrash();
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    
    public static void main(String args[]){
        System.out.println(MessageDigestUtils.convertToHexadecimalString(org.apache.commons.codec.binary.Base64.decodeBase64("ZJSe40EazoaGIMAzuq36Q5S9uEtwvSX5tYdMlrnM9Xo8Tk47v60QO2I0UEyasu2nDRgdjV632luzR6AjSMeQsw==")));
    }
}
