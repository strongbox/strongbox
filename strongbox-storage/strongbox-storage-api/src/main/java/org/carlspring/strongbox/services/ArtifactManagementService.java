package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.StreamUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryOutputStream;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

/**
 * @author mtodorov
 */
@Component
public class ArtifactManagementService
{
    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementService.class);

    @Inject
    protected ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    protected ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;

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
    protected RepositoryPathResolver repositoryPathResolver;
    
    @Inject
    protected RepositoryPathLock repositoryPathLock;

    @Transactional
    public long validateAndStore(RepositoryPath repositoryPath,
                                 InputStream is)
        throws IOException,
        ProviderImplementationException,
        NoSuchAlgorithmException,
        ArtifactCoordinatesValidationException
    {
        ReadWriteLock lock = repositoryPathLock.lock(repositoryPath);
        lock.writeLock().lock();

        try
        {
            performRepositoryAcceptanceValidation(repositoryPath);

            return doStore(repositoryPath, is);
        } 
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    @Deprecated
    @Transactional
    public long validateAndStore(String storageId,
                                 String repositoryId,
                                 String path,
                                 InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactCoordinatesValidationException
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);

        return validateAndStore(repositoryPath, is);
    }
    
    @Transactional
    public long store(RepositoryPath repositoryPath,
                      InputStream is)
        throws IOException
    {
        ReadWriteLock lockSource = repositoryPathLock.lock(repositoryPath);
        Lock lock = lockSource.writeLock();
        lock.lock();
        
        try
        {
            return doStore(repositoryPath, is);
        } 
        finally
        {
            lock.unlock();
        }
    }

    private long doStore(RepositoryPath repositoryPath,
                         InputStream is)
            throws IOException
    {
        long result;
        boolean updatedArtifactFile = false;

        if (RepositoryFiles.artifactExists(repositoryPath))
        {
            updatedArtifactFile = RepositoryFiles.isArtifact(repositoryPath);
        }
        
        try (final RepositoryOutputStream aos = artifactResolutionService.getOutputStream(repositoryPath))
        {
            result = writeArtifact(repositoryPath, is, aos);
        }
        catch (IOException e)
        {
           throw e; 
        }
        catch (Exception e)
        {
            throw new ArtifactStorageException(e);
        }

        if (updatedArtifactFile)
        {
            artifactEventListenerRegistry.dispatchArtifactUpdatedEvent(repositoryPath);
        }
        else
        {
            artifactEventListenerRegistry.dispatchArtifactStoredEvent(repositoryPath);
        }
        
        if (RepositoryFiles.isMetadata(repositoryPath))
        {
            artifactEventListenerRegistry.dispatchArtifactMetadataStoredEvent(repositoryPath);
        }

        
        return result;
    }

    private long writeArtifact(RepositoryPath repositoryPath,
                               InputStream is,
                               OutputStream os)
            throws IOException
    {
        ArtifactOutputStream aos = StreamUtils.findSource(ArtifactOutputStream.class, os);

        Repository repository = repositoryPath.getRepository();

        Boolean checksumAttribute = RepositoryFiles.isChecksum(repositoryPath);

        // If we have no digests, then we have a checksum to store.
        if (Boolean.TRUE.equals(checksumAttribute))
        {
            aos.setCacheOutputStream(new ByteArrayOutputStream());
        }

        if (repository.isHostedRepository())
        {
            artifactEventListenerRegistry.dispatchArtifactUploadingEvent(repositoryPath);
        }

        long totalAmountOfBytes = IOUtils.copy(is, os);

        Map<String, String> digestMap = aos.getDigestMap();
        if (Boolean.FALSE.equals(checksumAttribute) && !digestMap.isEmpty())
        {
            // Store artifact digests in cache if we have them.
            addChecksumsToCacheManager(digestMap, repositoryPath);
        }

        if (Boolean.TRUE.equals(checksumAttribute))
        {
            String checksumValue = ((ByteArrayOutputStream) aos.getCacheOutputStream()).toString(StandardCharsets.UTF_8.name());
            String algorithm = FilenameUtils.getExtension(repositoryPath.toString());
            String fileBaseName = FilenameUtils.getBaseName(repositoryPath.toString());

            checksumCacheManager.addArtifactChecksum(repositoryPath.resolveSibling(fileBaseName),
                                                     algorithm,
                                                     checksumValue);
        }

        return totalAmountOfBytes;
    }

    private void addChecksumsToCacheManager(Map<String, String> digestMap,
                                            RepositoryPath artifactPath)
    {
        digestMap.entrySet()
                 .stream()
                 .forEach(e -> checksumCacheManager.addArtifactChecksum(artifactPath, e.getKey(), e.getValue()));
    }

    private boolean performRepositoryAcceptanceValidation(RepositoryPath path)
            throws IOException, ProviderImplementationException, ArtifactCoordinatesValidationException
    {
        logger.info(String.format("Validate artifact with path [%s]", path));

        Repository repository = path.getFileSystem().getRepository();

        artifactOperationsValidator.validate(path);

        if (!RepositoryFiles.isArtifact(path))
        {
            return true;
        }
        
        ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(path);
        logger.info(String.format("Validate artifact with coordinates [%s]", coordinates));

        try
        {
            for (String validatorKey : repository.getArtifactCoordinateValidators().keySet())
            {
                ArtifactCoordinatesValidator validator = artifactCoordinatesValidatorRegistry.getProvider(
                        validatorKey);
                if (validator.supports(repository))
                {
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

        return true;
    }

    protected Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    @Transactional
    public void delete(RepositoryPath repositoryPath,
                       boolean force)
            throws IOException
    {
        artifactOperationsValidator.validate(repositoryPath);

        final Repository repository = repositoryPath.getRepository();

        artifactOperationsValidator.checkAllowsDeletion(repository);

        Optional<ArtifactEntry> artifactEntry = Optional.ofNullable(repositoryPath.getArtifactEntry());
        if (!Files.isDirectory(repositoryPath) && RepositoryFiles.isArtifact(repositoryPath) && !artifactEntry.isPresent())
        {
            throw new IOException(String.format("Corresponding [%s] record not found for path [%s]",
                                                ArtifactEntry.class.getSimpleName(), repositoryPath));
        }

        try
        {           
            RepositoryFiles.delete(repositoryPath, force);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    public void copy(RepositoryPath srcPath, RepositoryPath destPath)
            throws IOException
    {
        artifactOperationsValidator.validate(srcPath);

        if (Files.isDirectory(srcPath))
        {
            FileSystemUtils.copyRecursively(srcPath, destPath);
        }
        else
        {
            Files.copy(srcPath, destPath);
        }
    }

}
