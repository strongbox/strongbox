package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.api.jobs.ClearRepositoryTrashCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ClearRepositoryTrashCronJobTest
        extends TestCaseWithArtifactGeneration
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private JobManager jobManager;

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-tt");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-test-two");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/releases");

    private static Repository repository1;

    private static Repository repository2;

    private static Repository repository3;

    private static Artifact artifact1;

    private static Artifact artifact2;

    private static Artifact artifact3;

    private static boolean initialized;

    @Before
    public void setUp()
            throws Exception
    {
        if (!initialized)
        {
            repository1 = new Repository("releases-tt");
            repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository1.setTrashEnabled(true);
            Storage storage = configurationManagementService.getStorage("storage0");
            repository1.setStorage(storage);
            storage.saveRepository(repository1);
            repositoryManagementService.createRepository("storage0", "releases-tt");

            //Create released artifact
            String ga1 = "org.carlspring.strongbox.clear:strongbox-test-one";
            artifact1 = generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), ga1 + ":1.0:jar");

            repository2 = new Repository("releases-test-two");
            repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository2.setTrashEnabled(true);
            repository2.setStorage(storage);
            storage.saveRepository(repository2);
            repositoryManagementService.createRepository("storage0", "releases-test-two");

            String ga2 = "org.carlspring.strongbox.clear:strongbox-test-two";
            artifact2 = generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga2 + ":1.0:jar");

            //Create storage and repository for testing removing trash in storages
            Storage newStorage = new Storage("storage1");
            repository3 = new Repository("releases");
            repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository3.setTrashEnabled(true);
            repository3.setStorage(newStorage);
            configurationManagementService.saveStorage(newStorage);
            newStorage.saveRepository(repository3);
            repositoryManagementService.createRepository("storage1", "releases");

            //Create released artifact
            artifact3 = generateArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(), ga1 + ":1.0:jar");


            changeCreationDate(artifact1);
            changeCreationDate(artifact2);
            changeCreationDate(artifact3);

            initialized = true;
        }
    }

    public void addRebuildCronJobConfig(String name,
                                        String storageId,
                                        String repositoryId)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", ClearRepositoryTrashCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRebuildCronJobConfig(String name)
            throws Exception
    {
        List<CronTaskConfiguration> confs = cronTaskConfigurationService.getConfiguration(name);

        for (CronTaskConfiguration cnf : confs)
        {
            assertNotNull(cnf);
            cronTaskConfigurationService.deleteConfiguration(cnf);
        }

        assertNull(cronTaskConfigurationService.findOne(name));
    }

    @Test
    public void testRemoveTrashInRepository()
            throws Exception
    {
        File basedirTrash = repository1.getTrashDir();
        File[] dirs = basedirTrash.listFiles();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash isn't empty!", 0, dirs.length);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository1.getLayout());
        String path = "org/carlspring/strongbox/clear/strongbox-test-one/1.0";
        layoutProvider.delete("storage0", "releases-tt", path, false);

        dirs = basedirTrash.listFiles();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash is empty!", 1, dirs.length);

        String jobName = "RemoveTrash-1";

        addRebuildCronJobConfig(jobName, "storage0", "releases-tt");

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        dirs = basedirTrash.listFiles();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash isn't empty!", 0, dirs.length);

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRemoveTrashAllRepositories()
            throws Exception
    {
        final File basedirTrash1 = repository2.getTrashDir();
        File[] dirs1 = basedirTrash1.listFiles();

        assertTrue("There is no path to the repository trash!", dirs1 != null);
        assertEquals("The repository trash isn't empty!", 0, dirs1.length);

        LayoutProvider layoutProvider1 = layoutProviderRegistry.getProvider(repository2.getLayout());
        String path1 = "org/carlspring/strongbox/clear/strongbox-test-two/1.0";
        layoutProvider1.delete("storage0", "releases-test-two", path1, false);

        final File basedirTrash2 = repository3.getTrashDir();
        File[] dirs2 = basedirTrash2.listFiles();

        assertTrue("There is no path to the repository trash!", dirs2 != null);
        assertEquals("The repository trash isn't empty!", 0, dirs2.length);

        LayoutProvider layoutProvider2 = layoutProviderRegistry.getProvider(repository3.getLayout());
        String path2 = "org/carlspring/strongbox/clear/strongbox-test-one/1.0";
        layoutProvider2.delete("storage1", "releases", path2, false);

        dirs1 = basedirTrash1.listFiles();
        dirs2 = basedirTrash1.listFiles();

        assertTrue("There is no path to the repository trash!", dirs1 != null);
        assertEquals("The repository trash is empty!", 1, dirs1.length);
        assertTrue("There is no path to the repository trash!", dirs2 != null);
        assertEquals("The repository trash is empty!", 1, dirs2.length);

        String jobName = "RemoveTrash-2";

        addRebuildCronJobConfig(jobName, null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        dirs1 = basedirTrash1.listFiles();
        dirs2 = basedirTrash2.listFiles();

        assertTrue("There is no path to the repository trash!", dirs1 != null);
        assertEquals("The repository trash isn't empty!", 0, dirs1.length);
        assertTrue("There is no path to the repository trash!", dirs2 != null);
        assertEquals("The repository trash isn't empty!", 0, dirs2.length);

        deleteRebuildCronJobConfig(jobName);
    }

}
