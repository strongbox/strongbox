package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RegenerateNugetChecksumCronJobTestIT
        extends TestCaseWithNugetPackageGeneration
{

    private static final String STORAGE1 = "nuget-common-storage";

    private static final String STORAGE2 = "nuget-checksum-test";

    private static final String REPOSITORY_RELEASES = "rnccj-releases";

    private static final String REPOSITORY_ALPHA = "rnccj-alpha";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES);

    private static final File REPOSITORY_ALPHA_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                  "/storages/" + STORAGE1 + "/" +
                                                                  REPOSITORY_ALPHA);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE2 + "/" +
                                                                       REPOSITORY_RELEASES);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    private JobManager jobManager;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        //Create released nuget package in the repository rnccj-releases (storage1)
        generateNugetPackage(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                             "org.carlspring.strongbox.checksum-second", "1.0");

        createRepository(STORAGE1, REPOSITORY_ALPHA, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        //Create pre-released nuget package in the repository rnccj-alpha
        generateAlphaNugetPackage(REPOSITORY_ALPHA_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox.checksum-one",
                                  "1.0.1");

        createStorage(STORAGE2);

        createRepository(STORAGE2, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        //Create released nuget package in the repository rnccj-releases (storage2)
        generateNugetPackage(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(), "org.carlspring.strongbox.checksum-one",
                             "1.0");

    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_ALPHA));
        repositories.add(createRepositoryMock(STORAGE2, REPOSITORY_RELEASES));
        return repositories;
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

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_1 + "/org.carlspring.strongbox.checksum-second";

        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists());

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").length() >
                               0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512").length() > 0);

                    deleteRegenerateCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRegenerateCronJobConfig(jobName, STORAGE1, REPOSITORY_RELEASES, "org.carlspring.strongbox.checksum-second",
                                   false);
    }

    @Test
    public void testRegenerateNugetChecksumInRepository()
            throws Exception
    {
        String jobName = "RegenerateNuget-2";

        String artifactPath = REPOSITORY_ALPHA_BASEDIR + "/org.carlspring.strongbox.checksum-one";

        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512").exists());

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(artifactPath,
                                        "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(artifactPath,
                                        "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512").length() >
                               0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath,
                                        "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath,
                                        "/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512").length() >
                               0);

                    deleteRegenerateCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRegenerateCronJobConfig(jobName, STORAGE1, REPOSITORY_ALPHA, null, false);


    }

    @Test
    public void testRegenerateNugetChecksumInStorage()
            throws Exception
    {
        String jobName = "RegenerateNuget-3";

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_1 + "/org.carlspring.strongbox.checksum-second";

        FileUtils.deleteIfExists(
                new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists());

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").length() >
                               0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512").length() > 0);

                    deleteRegenerateCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRegenerateCronJobConfig(jobName, STORAGE1, null, null, false);
    }

    @Test
    public void testRegenerateNugetChecksumInStorages()
            throws Exception
    {
        String jobName = "RegenerateNuget-4";

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_2 + "/org.carlspring.strongbox.checksum-one";

        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512").exists());


        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512").length() > 0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.nuspec.sha512").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.nuspec.sha512").length() > 0);

                    deleteRegenerateCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRegenerateCronJobConfig(jobName, null, null, null, false);
    }

    private void createRepository(String storageId,
                                  String repositoryId,
                                  String policy,
                                  boolean indexing)
            throws IOException, JAXBException
    {
        Repository repository = new Repository(repositoryId);
        repository.setIndexingEnabled(indexing);
        repository.setPolicy(policy);
        repository.setLayout(RepositoryLayoutEnum.NUGET_HIERACHLICAL.getLayout());
        repository.setStorage(configurationManagementService.getStorage(storageId));

        createRepository(repository);
    }

    private void createRepository(Repository repository)
            throws IOException,
                   JAXBException
    {
        configurationManagementService.saveRepository(repository.getStorage()
                                                                .getId(), repository);

        // Create the repository
        repositoryManagementService.createRepository(repository.getStorage()
                                                               .getId(), repository.getId());
    }

    private void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new Storage(storageId));
    }

    private void createStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public static void cleanUp(Set<Repository> repositoriesToClean)
            throws Exception
    {
        if (repositoriesToClean != null)
        {
            for (Repository repository : repositoriesToClean)
            {
                removeRepositoryDirectory(repository.getStorage()
                                                    .getId(), repository.getId());
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
            org.apache.commons.io.FileUtils.deleteDirectory(repositoryBaseDir);
        }
    }

    public void removeRepositories(Set<Repository> repositoriesToClean)
            throws IOException, JAXBException
    {
        for (Repository repository : repositoriesToClean)
        {
            configurationManagementService.removeRepository(repository.getStorage()
                                                                      .getId(), repository.getId());
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

}

