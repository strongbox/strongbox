package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactResolutionServiceImpl
        implements ArtifactResolutionService
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String artifactPath)
        throws IOException,
        NoSuchAlgorithmException,
        ArtifactTransportException,
        ProviderImplementationException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Repository repository = getStorage(storageId).getRepository(repositoryId);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        ArtifactInputStream is = repositoryProvider.getInputStream(storageId, repositoryId, artifactPath);
        if (is == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " not found.");
        }

        return is;
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
        throws IOException,
        ProviderImplementationException,
        NoSuchAlgorithmException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        Repository repository = getStorage(storageId).getRepository(repositoryId);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        ArtifactOutputStream os = repositoryProvider.getOutputStream(storageId, repositoryId, artifactPath);
        if (os == null)
        {
            throw new ArtifactStorageException("Artifact " + artifactPath + " cannot be stored.");
        }

        return os;
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    @Override
    public ArtifactCoordinates getArtifactCoordinates(String storageId,
                                                      String repositoryId,
                                                      String artifactPath)
    {
        Repository repository = getStorage(storageId).getRepository(repositoryId);
        LayoutProvider<?> layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        layoutProvider.getArtifactCoordinates(artifactPath); 
        return layoutProvider.getArtifactCoordinates(artifactPath);
    }

}
