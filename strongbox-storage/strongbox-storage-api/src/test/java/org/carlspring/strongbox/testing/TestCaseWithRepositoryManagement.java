package org.carlspring.strongbox.testing;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

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

    public void createStorage(String storageId)
            throws IOException
    {
        createStorage(new Storage(storageId));
    }

    public void createStorage(Storage storage)
            throws IOException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public void createRepository(Repository repository)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);

        // Create the repository
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
    }

    public void createRepositoryWithFile(Repository repository, String path)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        createRepository(repository);
        createFile(repository, path);
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
        Repository repository = configurationManagementService.getRepository(storageId, repositoryId);

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
    }

}
