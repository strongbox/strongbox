package org.carlspring.strongbox.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.VersionValidatorService;
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

/**
 * @author Sergey Bespalov
 *
 */
//TODO: move all the common mehtod implementation here
public abstract class AbstractArtifactManagementService implements ArtifactManagementService
{
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractArtifactManagementService.class);


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
        performRepositoryAcceptanceValidation(storageId, repositoryId, path);

        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(path);
        
        store(repositoryPath, is);
    }

    @Override
    @Transactional
    public void store(RepositoryPath repositoryPath,
                      InputStream is)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        
        String artifactPathRelative = repositoryPath.getRepositoryRelative().toString();
        performRepositoryAcceptanceValidation(storage.getId(), repository.getId(), artifactPathRelative);

        try (final ArtifactOutputStream aos = (ArtifactOutputStream) Files.newOutputStream(repositoryPath))
        {
            doStore(repositoryPath, is, aos);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e);
        }
    }

    private void doStore(RepositoryPath repositoryPath,
                         InputStream is,
                         final ArtifactOutputStream aos)
            throws IOException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        
        String artifactPathRelative = repositoryPath.getRepositoryRelative().toString();
        String artifactPath = storage.getId() + "/" + repository.getId() + "/" + artifactPathRelative;
     
        Boolean checksumAttribute = (Boolean) Files.getAttribute(repositoryPath, RepositoryFileAttributes.CHECKSUM);
        
        // If we have no digests, then we have a checksum to store.
        if (Boolean.TRUE.equals(checksumAttribute))
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
                // Validate checksum with artifact digest cache.
                validateUploadedChecksumAgainstCache(checksumValue, artifactPath);
            }
        }

        storeArtifact(repositoryPath);
    }

    private void storeArtifact(RepositoryPath path)
    {
        Repository repository = path.getFileSystem().getRepository();
        Storage storage = repository.getStorage();

        ArtifactCoordinates coordinates = artifactResolutionService.getArtifactCoordinates(storage.getId(),
                                                                                           repository.getId(),
                                                                                           path.getRepositoryRelative()
                                                                                               .toString());

        ArtifactEntry artifactEntry = artifactEntryService.findOne(coordinates)
                                                          .orElse(createArtifactEntry(coordinates,
                                                                                      storage.getId(),
                                                                                      repository.getId()));
        artifactEntryService.save(artifactEntry);
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
    
    private boolean performRepositoryAcceptanceValidation(String storageId,
                                                          String repositoryId,
                                                          String path)
            throws IOException, ProviderImplementationException
    {
        logger.info(String.format("Validate artifact with path [%s]", path));
        
        artifactOperationsValidator.validate(storageId, repositoryId, path);

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        if (!path.contains("/maven-metadata.") &&
            !ArtifactUtils.isMetadata(path) && !ArtifactUtils.isChecksum(path))
        {
            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            ArtifactCoordinates coordinates = layoutProvider.getArtifactCoordinates(path);
            
            logger.info(String.format("Validate artifact with coordinates [%s]", coordinates));
            
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
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
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
            logger.debug("The requested path does not exist: /" + storageId + "/" + repositoryId + "/" + path);
        }

        return null;
    }

}
