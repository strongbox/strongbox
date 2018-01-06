package org.carlspring.strongbox.services;

import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.io.StreamUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyStrategyDeterminator;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.ArtifactChecksum;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;
import org.carlspring.strongbox.storage.validation.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mtodorov
 */
@Component
public class ArtifactManagementService implements ConfigurationService
{
    
    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementService.class);

    private Map<URI, Lock> pathUriMap = new ConcurrentHashMap<>();
    
    @Inject
    protected ArtifactOperationsValidator artifactOperationsValidator;
    
    @Inject
    protected VersionValidatorService versionValidatorService;
    
    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected ArtifactEntryService artifactEntryService;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected ArtifactResolutionService artifactResolutionService;

    @Inject
    protected ChecksumCacheManager checksumCacheManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    protected ArtifactByteStreamsCopyStrategyDeterminator artifactByteStreamsCopyStrategyDeterminator;

    
    @Transactional
    public long validateAndStore(String storageId,
                                 String repositoryId,
                                 String path,
                                 InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException
    {
        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(path);
        
        performRepositoryAcceptanceValidation(repositoryPath);        
        return store(repositoryPath, is);
    }
    
    @Transactional
    public long store(RepositoryPath repositoryPath,
                      InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        URI pathUri = repositoryPath.toUri();
        accureLock(pathUri);
        try (final RepositoryOutputStream aos = artifactResolutionService.getOutputStream(storage.getId(),
                                                                                          repository.getId(),
                                                                                          RepositoryFiles.stringValue(repositoryPath)))
        {
            long result = storeArtifact(repositoryPath, is, aos);
            return result;                
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e);
        } finally {
            releaseLock(pathUri);
        }
    }

    public void releaseLock(URI pathUri)
    {
        Lock lock = pathUriMap.remove(pathUri);
        lock.unlock();
    }

    public void accureLock(URI pathUri)
    {
        Lock lock = Optional.ofNullable(pathUriMap.putIfAbsent(pathUri, lock = new ReentrantLock())).orElse(lock);
        lock.lock();
        while (!pathUriMap.containsKey(pathUri))
        {
            lock = Optional.ofNullable(pathUriMap.putIfAbsent(pathUri, lock = new ReentrantLock())).orElse(lock);
            lock.lock();
        }
    }

    private long storeArtifact(RepositoryPath repositoryPath,
                               InputStream is,
                               OutputStream os)
            throws IOException,
                   ProviderImplementationException
    {
        ArtifactOutputStream aos = StreamUtils.findSource(ArtifactOutputStream.class, os);
        
        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();

        String repositoryId = repository.getId();
        String storageId = storage.getId();

        String artifactPathRelative = RepositoryFiles.stringValue(repositoryPath);
        String artifactPath = storageId + "/" + repositoryId + "/" + artifactPathRelative;

        Boolean checksumAttribute = RepositoryFiles.isChecksum(repositoryPath);
        
        boolean updatedMetadataFile = false;
        boolean updatedArtifactFile = false;
        boolean updatedArtifactChecksumFile = false;
        if (Files.exists(repositoryPath))
        {
            if (RepositoryFiles.isMetadata(repositoryPath))
            {
                updatedMetadataFile = true;
            }
            else if (checksumAttribute)
            {
                updatedArtifactChecksumFile = true;
            }
            else
            {
                updatedArtifactFile = true;
            }
        }
        
        // If we have no digests, then we have a checksum to store.
        if (Boolean.TRUE.equals(checksumAttribute))
        {
            aos.setCacheOutputStream(new ByteArrayOutputStream());
        }

        if (repository.isHostedRepository())
        {
            artifactEventListenerRegistry.dispatchArtifactUploadingEvent(storageId, repositoryId, artifactPath);
        }
        else
        {
            artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(storageId, repositoryId, artifactPath);
        }

        long totalAmountOfBytes = artifactByteStreamsCopyStrategyDeterminator.determine(repository).copy(is, os,
                                                                                                         repositoryPath);

        if (updatedMetadataFile)
        {
            // If this is a metadata file and it has been updated:
            artifactEventListenerRegistry.dispatchArtifactMetadataFileUpdatedEvent(storageId,
                                                                                   repositoryId,
                                                                                   artifactPath);

            artifactEventListenerRegistry.dispatchArtifactMetadataFileUploadedEvent(storageId,
                                                                                    repositoryId,
                                                                                    artifactPath);
        }
        if (updatedArtifactChecksumFile)
        {
            // If this is a checksum file and it has been updated:
            artifactEventListenerRegistry.dispatchArtifactChecksumFileUpdatedEvent(storageId,
                                                                                   repositoryId,
                                                                                   artifactPath);
        }

        if (updatedArtifactFile)
        {
            // If this is an artifact file and it has been updated:
            artifactEventListenerRegistry.dispatchArtifactUploadedEvent(storageId, repositoryId, artifactPath);
        }

        Map<String, String> digestMap = aos.getDigestMap();
        if (Boolean.FALSE.equals(checksumAttribute) && !digestMap.isEmpty())
        {
            // Store artifact digests in cache if we have them.
            addChecksumsToCacheManager(digestMap, artifactPath);
        }
        
        if (Boolean.TRUE.equals(checksumAttribute))
        {
            byte[] checksumValue = ((ByteArrayOutputStream) aos.getCacheOutputStream()).toByteArray();
            if (checksumValue != null && checksumValue.length > 0)
            {
                artifactEventListenerRegistry.dispatchArtifactChecksumUploadedEvent(storageId,
                                                                                    repositoryId,
                                                                                    artifactPath);

                // Validate checksum with artifact digest cache.
                validateUploadedChecksumAgainstCache(checksumValue, artifactPath);
            }
        }

        return totalAmountOfBytes;
    }

    private void validateUploadedChecksumAgainstCache(byte[] checksum,
                                                      String artifactPath)
            throws ArtifactStorageException
    {
        logger.debug("Received checksum: " + new String(checksum, StandardCharsets.UTF_8));

        String artifactBasePath = artifactPath.substring(0, artifactPath.lastIndexOf('.'));
        String checksumExtension = artifactPath.substring(artifactPath.lastIndexOf('.') + 1, artifactPath.length());

        if (!matchesChecksum(checksum, artifactBasePath, checksumExtension))
        {
            logger.error(String.format("The checksum for %s [%s] is invalid!", artifactPath, new String(checksum, StandardCharsets.UTF_8)));
        }

        checksumCacheManager.removeArtifactChecksum(artifactBasePath, checksumExtension);
    }

    private boolean matchesChecksum(byte[] pChecksum,
                                    String artifactBasePath,
                                    String checksumExtension)
    {
        String checksum = new String(pChecksum, StandardCharsets.UTF_8);
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
                                   checksum));

        return matched != null && !matched.isEmpty();
    }
    
    private void addChecksumsToCacheManager(Map<String, String> digestMap,
                                            String artifactPath)
    {
        digestMap.entrySet()
                 .stream()
                 .forEach(e -> checksumCacheManager.addArtifactChecksum(artifactPath, e.getKey(), e.getValue()));
    }
    
    private boolean performRepositoryAcceptanceValidation(RepositoryPath path)
            throws IOException, ProviderImplementationException
    {
        logger.info(String.format("Validate artifact with path [%s]", path));
        
        Repository repository = path.getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        
        artifactOperationsValidator.validate(storage.getId(), repository.getId(), path.relativize().toString());

        if (!RepositoryFiles.isMetadata(path) && !RepositoryFiles.isChecksum(path))
        {
            ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(path);
            logger.info(String.format("Validate artifact with coordinates [%s]", coordinates));
            
            try
            {
                final Set<VersionValidator> validators = versionValidatorService.getVersionValidators();
                for (VersionValidator validator : validators)
                {
                    if (validator.supports(repository)) {
                        validator.validate(repository, coordinates);
                    }
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
    
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }
    
    public InputStream resolve(String storageId,
                               String repositoryId,
                               String path)
            throws IOException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        try
        {
            return artifactResolutionService.getInputStream(storageId, repositoryId, path);
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            // This is not necessarily an error. It could simply be a check
            // whether a resource exists, before uploading/updating it.
            logger.debug("The requested path does not exist: /" + storageId + "/" + repositoryId + "/" + path);
        }

        return null;
    }
    
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
    
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
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
    }

    public RepositoryPath getPath(String storageId,
                                  String repositoryId,
                                  String path) 
             throws IOException
    {
        return artifactResolutionService.getPath(storageId, repositoryId, path);
        
    }
    
}
