package org.carlspring.strongbox.services.impl;

import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.locator.handlers.RemoveTimestampedSnapshotOperation;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.ArtifactChecksum;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mtodorov
 */
@Component("mavenArtifactManagementService")
public class MavenArtifactManagementService
    implements ArtifactManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactManagementService.class);

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private VersionValidatorService versionValidatorService;

    @Inject
    private ChecksumCacheManager checksumCacheManager;

    @Inject
    private MavenSnapshotManager mavenSnapshotManager;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Inject
    private ArtifactEntryService artifactEntryService;


    @Override
    @Transactional
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException
    {
        store(storageId, repositoryId, path, is, null);
    }

    @Override
    @Transactional
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is,
                      OutputStream os)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException
    {
        String artifactPath = storageId + "/" + repositoryId + "/" + path;
        performRepositoryAcceptanceValidation(storageId, repositoryId, path);

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        try (final ArtifactOutputStream aos = os != null ?
                                        (os instanceof ArtifactOutputStream ?
                                         (ArtifactOutputStream) os :
                                         new ArtifactOutputStream(os, layoutProvider.getArtifactCoordinates(path))) :
                                        artifactResolutionService.getOutputStream(storageId, repositoryId, path))
        {
            // If we have no digests, then we have a checksum to store.
            if (ArtifactUtils.isChecksum(path))
            {
                aos.setCacheOutputStream(new ByteArrayOutputStream());
            }

            int readLength;
            byte[] bytes = new byte[4096];
            while ((readLength = is.read(bytes, 0, bytes.length)) != -1)
            {
                // Write the artifact
                aos.write(bytes, 0, readLength);
                aos.flush();
            }

            if (ArtifactUtils.isChecksum(path))
            {
                if (!aos.getDigestMap().isEmpty())
                {
                    // Store artifact digests in cache if we have them.
                    addChecksumsToCacheManager(aos.getDigestMap(), artifactPath);
                }

                byte[] checksum = ((ByteArrayOutputStream) aos.getCacheOutputStream()).toByteArray();
                if (checksum != null && checksum.length > 0)
                {
                    // Validate checksum with artifact digest cache.
                    validateUploadedChecksumAgainstCache(checksum, artifactPath);
                }
            }

            if (ArtifactUtils.isArtifact(path))
            {
                addArtifactToIndex(storageId, repositoryId, path);
            }

            storeArtifact(storageId, repositoryId, path);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e);
        }
    }

    private void storeArtifact(String storageId,
                               String repositoryId,
                               String path)
    {
        ArtifactCoordinates artifactCoordinates = artifactResolutionService.getArtifactCoordinates(storageId,
                                                                                                   repositoryId,
                                                                                                   path);

        ArtifactEntry artifactEntry = artifactEntryService.findOne(artifactCoordinates)
                                                          .orElse(createArtifactEntry(artifactCoordinates,
                                                                                      storageId,
                                                                                      repositoryId));
        artifactEntryService.save(artifactEntry);
    }

    private ArtifactEntry createArtifactEntry(ArtifactCoordinates artifactCoordinates,
                                              String storageId,
                                              String repositoryId)
    {
        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);
        artifactEntry.setArtifactCoordinates(artifactCoordinates);

        return artifactEntry;
    }

    private void addArtifactToIndex(String storageId,
                                    String repositoryId,
                                    String path)
        throws IOException
    {
        Storage storage = getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        if (!repository.isIndexingEnabled() || !ArtifactFileUtils.isArtifactFile(path) || indexer == null)
        {
            return;
        }

        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);

        File storageBasedir = new File(storage.getBasedir());
        File artifactFile = new File(new File(storageBasedir, repositoryId), path).getCanonicalFile();

        indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
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
            ArtifactCoordinates coordinates = layoutProvider.getArtifactCoordinates(path);

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
        }
        catch (IOException | ProviderImplementationException | SearchException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public boolean contains(String storageId, String repositoryId, String artifactPath)
            throws IOException
    {
        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);

            return layoutProvider.contains(storageId, repositoryId, artifactPath);
        }
        catch (IOException | ProviderImplementationException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
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
        }
        else
        {
            FileUtils.copyFile(srcFile, destFile);
        }

        addArtifactToIndex(destStorageId, destRepositoryId, path);
    }

    private void validateUploadedChecksumAgainstCache(byte[] checksum,
                                                      String artifactPath)
            throws ArtifactStorageException
    {
        logger.debug("Received checksum: " + new String(checksum));

        String artifactBasePath = artifactPath.substring(0, artifactPath.lastIndexOf('.'));
        String checksumExtension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1, artifactPath.length());

        if (!matchesChecksum(checksum, artifactBasePath, checksumExtension))
        {
            logger.error(String.format("The checksum for %s [%s] is invalid!", artifactPath, new String(checksum)));
        }

        checksumCacheManager.removeArtifactChecksum(artifactBasePath);
    }

    private boolean matchesChecksum(byte[] pChecksum,
                                    String artifactBasePath,
                                    String checksumExtension)
    {
        String checksum = new String(pChecksum);
        ArtifactChecksum artifactChecksum = checksumCacheManager.getArtifactChecksum(artifactBasePath);

        if (artifactChecksum == null)
        {
            return false;
        }

        Map<Boolean, Set<String>> matchingMap = artifactChecksum.getChecksums()
                                                                .entrySet()
                                                                .stream()
                                                                .collect(Collectors.groupingBy(e -> e.getValue()
                                                                                                     .equals(checksum),
                                                                                               Collectors.mapping(
                                                                                                       e -> e.getKey(),
                                                                                                       Collectors.toSet())));

        Set<String> matched = matchingMap.get(Boolean.TRUE);
        Set<String> unmatched = matchingMap.get(Boolean.FALSE);

        logger.debug(String.format("Artifact checksum matchings: artifact-[%s]; ext-[%s]; matched-[%s];" +
                                   " unmatched-[%s]; checksum-[%s]",
                                   artifactBasePath,
                                   checksumExtension,
                                   matched,
                                   unmatched,
                                   new String(checksum)));

        return matched != null && !matched.isEmpty();
    }

    private void addChecksumsToCacheManager(Map<String, String> digestMap,
                                            String artifactPath)
    {
        digestMap.entrySet()
                 .stream()
                 .forEach(e -> checksumCacheManager.addArtifactChecksum(artifactPath, e.getKey(), e.getValue()));
    }

    @Override
    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           int keepPeriod)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (repository.getPolicy()
                      .equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
        {
            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            RepositoryPath repositoryPath = layoutProvider.resolve(repository, artifactPath);
            
            RemoveTimestampedSnapshotOperation operation = new RemoveTimestampedSnapshotOperation(mavenSnapshotManager);
            operation.setStorage(storage);
            operation.setBasePath(repositoryPath);
            operation.setNumberToKeep(numberToKeep);
            operation.setKeepPeriod(keepPeriod);

            ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
            locator.setOperation(operation);
            locator.locateArtifactDirectories();
        }
        else
        {
            throw new ArtifactStorageException("Type of repository is invalid: repositoryId - " + repositoryId);
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

}
