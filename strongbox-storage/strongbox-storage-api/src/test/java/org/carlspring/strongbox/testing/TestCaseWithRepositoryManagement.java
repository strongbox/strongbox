package org.carlspring.strongbox.testing;

import java.io.IOException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author carlspring
 */
public class TestCaseWithRepositoryManagement extends TestCaseWithRepository
{

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    protected RepositoryManagementService repositoryManagementService;
    

    public void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new Storage(storageId));
    }

    public void createStorage(Storage storage)
            throws IOException, JAXBException
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
}
