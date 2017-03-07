package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.api.jobs.RegenerateChecksumCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.util.FileUtils;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RegenerateNugetChecksumCronJobTest
        extends TestCaseWithNugetPackageGeneration
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private JobManager jobManager;

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/nuget-alpha");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/nuget-releases");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/nuget-releases");

    private static final String NUGET_PACKAGE_BASE_ID = "org.carlspring.strongbox.checksum-one";

    private static final String STORAGE_BASE_ID = "storage0";

    private static boolean initialized;

    @Before
    public void setUp()
            throws Exception
    {
        if (!initialized)
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR_1.mkdirs();
            REPOSITORY_BASEDIR_2.mkdirs();
            REPOSITORY_BASEDIR_3.mkdirs();

            Storage storage = configurationManagementService.getStorage(STORAGE_BASE_ID);

            //Create repository nuget-alpha in storage0
            createRepository(storage, "nuget-alpha", RepositoryPolicyEnum.SNAPSHOT.getPolicy());

            //Create repository nuget-releases in storage0
            createRepository(storage, "nuget-releases", RepositoryPolicyEnum.RELEASE.getPolicy());

            //Create a new storage with id "storage1" and repository nuget-releases in it
            Storage storage1 = new Storage("storage1");
            configurationManagementService.addOrUpdateStorage(storage1);
            createRepository(storage1, "nuget-releases", RepositoryPolicyEnum.RELEASE.getPolicy());

            //Create pre-released nuget package in the repository nuget-alpha
            generateAlphaNugetPackage(REPOSITORY_BASEDIR_1.getAbsolutePath() + "/org.carlspring.strongbox.checksum-one",
                                      NUGET_PACKAGE_BASE_ID, "1.0.1");

            //Create released nuget package in the repository nuget-releases (storage0)
            generateNugetPackage(REPOSITORY_BASEDIR_2.getAbsolutePath() + "/org.carlspring.strongbox.checksum-second",
                                 "org.carlspring.strongbox.checksum-second", "1.0");

            //Create released nuget package in the repository nuget-releases (storage1)
            generateNugetPackage(REPOSITORY_BASEDIR_3.getAbsolutePath() + "/org.carlspring.strongbox.checksum-one",
                                 NUGET_PACKAGE_BASE_ID, "1.0");

            initialized = true;
        }
    }

    public void addRegenerateCronJobConfig(String name,
                                           String storageId,
                                           String repositoryId,
                                           String basePath,
                                           boolean forceRegeneration)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RegenerateChecksumCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);
        cronTaskConfiguration.addProperty("forceRegeneration", String.valueOf(forceRegeneration));

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRegenerateCronJobConfig(String name)
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
    public void testRegenerateNugetPackageChecksum()
            throws Exception
    {
        String jobName = "RegenerateNuget-1";

        String artifactPath = REPOSITORY_BASEDIR_2 + "/org.carlspring.strongbox.checksum-second";

        FileUtils.deleteIfExists(
                new File(artifactPath,
                         "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").exists());

        addRegenerateCronJobConfig(jobName, "storage0", "nuget-releases", "org.carlspring.strongbox.checksum-second",
                                   false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").length() >
                   0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateNugetChecksumInRepository()
            throws Exception
    {
        String jobName = "RegenerateNuget-2";

        String artifactPath = REPOSITORY_BASEDIR_1 + "/org.carlspring.strongbox.checksum-one";

        FileUtils.deleteIfExists(
                new File(artifactPath,
                         "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".1.0.1-alpha" + ".nupkg.sha512"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".1.0.1-alpha" +
                             ".nupkg.sha512").exists());

        addRegenerateCronJobConfig(jobName, "storage0", "nuget-alpha", null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".1.0.1-alpha" +
                            ".nupkg.sha512").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".1.0.1-alpha" +
                            ".nupkg.sha512").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath,
                            "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath,
                            "/1.0.1-alpha/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateNugetChecksumInStorage()
            throws Exception
    {
        String jobName = "RegenerateNuget-3";

        String artifactPath = REPOSITORY_BASEDIR_2 + "/org.carlspring.strongbox.checksum-second";

        FileUtils.deleteIfExists(
                new File(artifactPath,
                         "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").exists());

        addRegenerateCronJobConfig(jobName, "storage0", null, null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".1.0" + ".nupkg.sha512").length() >
                   0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-second" + ".nuspec.sha512").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateNugetChecksumInStorages()
            throws Exception
    {
        String jobName = "RegenerateNuget-4";

        String artifactPath = REPOSITORY_BASEDIR_3 + "/org.carlspring.strongbox.checksum-one";

        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".1.0" + ".nupkg.sha512"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".1.0" + ".nupkg.sha512").exists());

        addRegenerateCronJobConfig(jobName, null, null, null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".1.0" + ".nupkg.sha512").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".1.0" + ".nupkg.sha512").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath,
                            "/1.0/" + "org.carlspring.strongbox.checksum-one" + ".nuspec.sha512").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    private void createRepository(Storage storage,
                                  String repositoryId,
                                  String repositoryPolicy)
            throws IOException, JAXBException
    {
        Repository repository = new Repository(repositoryId);
        repository.setPolicy(repositoryPolicy);
        repository.setLayout(RepositoryLayoutEnum.NUGET_HIERACHLICAL.getLayout());
        repository.setStorage(storage);

        storage.addOrUpdateRepository(repository);
        repositoryManagementService.createRepository(storage.getId(), repositoryId);

    }

}

