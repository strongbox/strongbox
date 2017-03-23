package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * @author carlspring
 */
public class TestCaseWithRepository
{

    public static final String STORAGE0 = "storage0";

    @Inject
    ConfigurationManagementService configurationManagementService;


    public static void cleanUp(Set<Repository> repositoriesToClean)
            throws Exception
    {
        if (repositoriesToClean != null)
        {
            for (Repository repository : repositoriesToClean)
            {
                removeRepositoryDirectory(repository.getStorage().getId(), repository.getId());
            }
        }
    }

    private static void removeRepositoryDirectory(String storageId,
                                                  String repositoryId)
            throws IOException
    {
        File repositoryBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory(),
                                          "/storages/" + storageId + "/" + repositoryId);

        if (repositoryBaseDir.exists())
        {
            FileUtils.deleteDirectory(repositoryBaseDir);
        }
    }

    public void removeRepositories(Set<Repository> repositoriesToClean)
            throws IOException, JAXBException
    {
        for (Repository repository : repositoriesToClean)
        {
            configurationManagementService.removeRepository(repository.getStorage().getId(), repository.getId());
            if (repository.isIndexingEnabled())
            {
                // repositoryIndexManager.closeIndexersForRepository(repository.getStorage().getId(), repository.getId());
            }
        }
    }

    public static Repository createRepositoryMock(String storageId,
                                                  String repositoryId)
    {
        // This is no the real storage, but has a matching ID.
        // We're mocking it, as the configurationManager is not available at the the static methods are invoked.
        Storage storage = new Storage(storageId);

        Repository repository = new Repository(repositoryId);
        repository.setStorage(storage);

        return repository;
    }

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
