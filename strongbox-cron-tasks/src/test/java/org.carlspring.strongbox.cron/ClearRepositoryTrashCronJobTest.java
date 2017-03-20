package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.configuration.ConfigurationManager;
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
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.BeforeClass;
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
        extends TestCaseWithArtifactGenerationAndIndexing
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES_1 = "crtcj-releases";

    private static final String REPOSITORY_RELEASES_2 = "crtcj-releases-test";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_2);

    private static final File REPOSITORY_RELEASES_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static Repository repository1;

    private static Repository repository2;

    private static Repository repository3;

    private static boolean initialized;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private JobManager jobManager;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @PostConstruct
    public void initialize()
            throws Exception
    {
        if (!initialized)
        {
            repository1 = new Repository(REPOSITORY_RELEASES_1);
            repository1.setStorage(configurationManager.getConfiguration()
                                                       .getStorage(STORAGE0));
            repository1.setAllowsForceDeletion(false);
            repository1.setTrashEnabled(true);
            repository1.setIndexingEnabled(false);
            createRepository(repository1);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                             "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");

            repository2 = new Repository(REPOSITORY_RELEASES_2);
            repository2.setStorage(configurationManager.getConfiguration()
                                                       .getStorage(STORAGE0));
            repository2.setAllowsForceDeletion(false);
            repository2.setTrashEnabled(true);
            repository2.setIndexingEnabled(false);
            createRepository(repository2);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                             "org.carlspring.strongbox.clear:strongbox-test-two:1.0:jar");

            createStorage(new Storage(STORAGE1));

            repository3 = new Repository(REPOSITORY_RELEASES_1);
            repository3.setStorage(configurationManager.getConfiguration()
                                                       .getStorage(STORAGE1));
            repository3.setAllowsForceDeletion(false);
            repository3.setTrashEnabled(true);
            repository3.setIndexingEnabled(false);
            createRepository(repository3);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                             "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");

            initialized = true;
        }
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1));
        return repositories;
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
        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES_1, path, false);

        dirs = basedirTrash.listFiles();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash is empty!", 1, dirs.length);

        String jobName = "RemoveTrash-1";

        addRebuildCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES_1);

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
        layoutProvider1.delete(STORAGE0, REPOSITORY_RELEASES_2, path1, false);

        final File basedirTrash2 = repository3.getTrashDir();
        File[] dirs2 = basedirTrash2.listFiles();

        assertTrue("There is no path to the repository trash!", dirs2 != null);
        assertEquals("The repository trash isn't empty!", 0, dirs2.length);

        LayoutProvider layoutProvider2 = layoutProviderRegistry.getProvider(repository3.getLayout());
        String path2 = "org/carlspring/strongbox/clear/strongbox-test-one/1.0";
        layoutProvider2.delete(STORAGE1, REPOSITORY_RELEASES_1, path2, false);

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
