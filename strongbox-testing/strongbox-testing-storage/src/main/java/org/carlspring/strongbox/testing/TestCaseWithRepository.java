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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;

/**
 * @author carlspring
 */
public class TestCaseWithRepository
{

    public static final String STORAGE0 = "storage0";

    protected List<File> generatedArtifacts = new ArrayList<>();

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @ClassRule
    public static JUnitHelper jUnitHelper = new JUnitHelper();


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
            /*
            if (repository.isIndexingEnabled())
            {
                repositoryIndexManager.closeIndexersForRepository(repository.getStorage().getId(), repository.getId());
            }
            */
        }
    }

    public static Repository createRepositoryMock(String storageId,
                                                  String repositoryId)
    {
        // This is not the real storage, but has a matching ID.
        // We're mocking it, as the configurationManager is not available at the the static methods are invoked.
        Storage storage = new Storage(storageId);

        Repository repository = new Repository(repositoryId);
        repository.setStorage(storage);

        return repository;
    }

    public void generateArtifact(String artifactPath)
            throws IOException
    {
        File f = new File(artifactPath);

        generatedArtifacts.add(f);

        if (!f.exists())
        {
            if (!f.getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                f.getParentFile().mkdirs();
            }

            //noinspection ResultOfMethodCallIgnored
            f.createNewFile();
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
