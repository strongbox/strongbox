package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
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
import org.springframework.web.util.UriComponentsBuilder;

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
    public RepositoryInputStream getInputStream(String storageId,
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

        RepositoryInputStream is = repositoryProvider.getInputStream(storageId, repositoryId, artifactPath);
        if (is == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " not found.");
        }

        return is;
    }

    @Override
    public RepositoryOutputStream getOutputStream(String storageId,
                                                  String repositoryId,
                                                  String artifactPath)
        throws IOException,
        ProviderImplementationException,
        NoSuchAlgorithmException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        Repository repository = getStorage(storageId).getRepository(repositoryId);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryOutputStream os = repositoryProvider.getOutputStream(storageId, repositoryId, artifactPath);
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
    public URL resolveResource(String storageId,
                               String repositoryId,
                               String path)
            throws IOException
    {
        URI baseUri = configurationManager.getBaseUri();

        Repository repository = getStorage(storageId).getRepository(repositoryId);
        LayoutProvider<?> layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        URI artifactResource = layoutProvider.resolveResource(repository, path);

        return UriComponentsBuilder.fromUri(baseUri)
                                   .pathSegment("storages", storageId, repositoryId, "/")
                                   .build()
                                   .toUri()
                                   .resolve(artifactResource)
                                   .toURL();
    }
    
    @Override
    public RepositoryPath resolvePath(String storageId,
                                      String repositoryId,
                                      String artifactPath) 
           throws IOException
    {        
        final Repository repository = getStorage(storageId).getRepository(repositoryId);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = (RepositoryPath)repositoryProvider.resolvePath(storageId, repositoryId, artifactPath);

        return resolvedPath;
    }
    
}
