package org.carlspring.strongbox.testing;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * @author carlspring
 */
public abstract class TestCaseWithRepositoryManagement extends TestCaseWithRepository
{

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    protected RepositoryManagementService repositoryManagementService;
    
    @Inject
    protected HostedRepositoryProvider hostedRepositoryProvider;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;
    
    public void createStorage(String storageId)
            throws IOException
    {
        createStorage(new MutableStorage(storageId));
    }

    public void createStorage(MutableStorage storage)
            throws IOException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public void createRepository(String storageId,
                                 MutableRepository repository)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repository.getId());
    }

    public void createRepositoryWithFile(MutableRepository repository,
                                         String storageId,
                                         String path)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        createRepository(storageId, repository);
        createFile(new Repository(repository), path);
    }
    
    public abstract void createProxyRepository(String storageId,
                                               String repositoryId,
                                               String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException;

    public void createFile(String storageId,
                           String repositoryId,
                           String path)
            throws IOException
    {
        Repository repository = configurationManagementService.getConfiguration().getRepository(storageId, repositoryId);

        createFile(repository, path);
    }

    public void createFile(Repository repository,
                           String path)
            throws IOException
    {       
        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
        Files.createDirectories(repositoryPath.getParent());
        
        createRandomSizeFile(repositoryPath);
    }

    private void createRandomSizeFile(RepositoryPath repositoryPath)
        throws IOException
    {
        try (OutputStream fos = hostedRepositoryProvider.getOutputStream(repositoryPath))
        {
            try (RandomInputStream ris = new RandomInputStream(true, 1000000))
            {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = ris.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, len);
                }
            }
        }
        
        artifactEventListenerRegistry.dispatchArtifactStoredEvent(repositoryPath);
    }

}
