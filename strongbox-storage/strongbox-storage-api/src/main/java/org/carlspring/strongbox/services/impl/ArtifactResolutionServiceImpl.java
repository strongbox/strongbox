package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactNotFoundException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryOutputStream;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.services.ArtifactResolutionService;
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
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    public RepositoryInputStream getInputStream(RepositoryPath path)
        throws IOException
    {
        Repository repository = path.getFileSystem().getRepository();
        artifactOperationsValidator.validate(path);
        
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        return (RepositoryInputStream) repositoryProvider.getInputStream(path);
    }

    @Override
    public RepositoryOutputStream getOutputStream(RepositoryPath repositoryPath)
        throws IOException,
        NoSuchAlgorithmException
    {
        artifactOperationsValidator.validate(repositoryPath);

        Repository repository = repositoryPath.getRepository();
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryOutputStream os = (RepositoryOutputStream) repositoryProvider.getOutputStream(repositoryPath);
        if (os == null)
        {
            throw new ArtifactStorageException("Artifact " + repositoryPath + " cannot be stored.");
        }

        return os;
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    @Override
    public RepositoryPath resolvePath(String storageId,
                                      String repositoryId,
                                      String artifactPath) 
           throws IOException
    {        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, artifactPath);

        Repository repository = repositoryPath.getRepository();
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        
        try
        {
            return (RepositoryPath)repositoryProvider.fetchPath(repositoryPath);
        }
        catch (ArtifactNotFoundException e)
        {
            return null;
        }
    }
    
}
