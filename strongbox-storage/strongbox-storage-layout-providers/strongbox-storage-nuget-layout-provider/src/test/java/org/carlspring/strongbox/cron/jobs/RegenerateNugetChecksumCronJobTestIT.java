package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.NugetLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.NugetRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = NugetLayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
public class RegenerateNugetChecksumCronJobTestIT
        extends BaseCronJobWithNugetIndexingTestCase
{

    private static final String STORAGE1 = "storage-nuget";

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

    @Inject
    private NugetRepositoryFactory nugetRepositoryFactory;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy());

        //Create released nuget package in the repository rnccj-releases (storage1)
        generateNugetPackage(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                             "org.carlspring.strongbox.checksum-second", "1.0");

        createRepository(STORAGE1, REPOSITORY_ALPHA, RepositoryPolicyEnum.SNAPSHOT.getPolicy());

        //Create pre-released nuget package in the repository rnccj-alpha
        generateAlphaNugetPackage(REPOSITORY_ALPHA_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox.checksum-one",
                                  "1.0.1");

        createStorage(STORAGE2);

        createRepository(STORAGE2, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy());

        //Create released nuget package in the repository rnccj-releases (storage2)
        generateNugetPackage(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(), "org.carlspring.strongbox.checksum-one",
                             "1.0");
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
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
        CronTaskConfigurationDto cronTaskConfiguration = new CronTaskConfigurationDto();
        cronTaskConfiguration.setOneTimeExecution(true);
        cronTaskConfiguration.setImmediateExecution(true);
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RegenerateChecksumCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 11 11 11 11 ? 2100");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);
        cronTaskConfiguration.addProperty("forceRegeneration", String.valueOf(forceRegeneration));

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfigurationDto obj = cronTaskConfigurationService.getTaskConfigurationDto(name);
        assertNotNull(obj);
    }

    @Test
    public void testRegenerateNugetPackageChecksum()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_1 + "/org.carlspring.strongbox.checksum-second";

        deleteIfExists(
                new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertTrue(!new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists(),
                   "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (!jobName1.equals(jobName) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
            resultList.add(new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE1, REPOSITORY_RELEASES,
                         properties ->
                         {
                             properties.put("basePath", "org.carlspring.strongbox.checksum-second");
                             properties.put("forceRegeneration","false");
                         });

        assertTrue(expectEvent(), "Failed to execute task!");
    
        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(),
                       "The checksum file doesn't exist!");
            assertTrue(f.length() > 0,
                       "The checksum file is empty!");
        });    
    }

    @Test
    public void testRegenerateNugetChecksumInRepository()
        throws Exception
    {
        final String jobName = expectedJobName;

        deleteIfExists(
                                 new File(REPOSITORY_ALPHA_BASEDIR,
                                         "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512"));
        deleteIfExists(
                                 new File(REPOSITORY_ALPHA_BASEDIR,
                                         "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertTrue(!new File(REPOSITORY_ALPHA_BASEDIR,
                             "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512").exists(),
                   "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobName, (jobName1,
                                                       statusExecuted) -> {
            if (!jobName1.equals(jobName) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(REPOSITORY_ALPHA_BASEDIR,
                    "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512"));
            resultList.add(new File(REPOSITORY_ALPHA_BASEDIR,
                    "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512"));
        });
        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE1, REPOSITORY_ALPHA,
                         properties -> properties.put("forceRegeneration", "false"));
        
        assertTrue(expectEvent(), "Failed to execute task!");
        
        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(),
                       "The checksum file doesn't exist!");
            assertTrue(f.length() > 0,
                       "The checksum file is empty!");
        });

    }

    @Test
    public void testRegenerateNugetChecksumInStorage()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_1 + "/org.carlspring.strongbox.checksum-second";

        deleteIfExists(new File(artifactPath,
                                              "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath,
                                              "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertTrue(!new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512").exists(),
                   "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobName, (jobName1,
                                                       statusExecuted) -> {
            if (!jobName1.equals(jobName) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(artifactPath,
                    "/1.0/org.carlspring.strongbox.checksum-second.1.0.nupkg.sha512"));
            resultList.add(new File(artifactPath,
                    "/1.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE1, null,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue(expectEvent(), "Failed to execute task!");

        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(),
                       "The checksum file doesn't exist!");
            assertTrue(f.length() > 0,
                       "The checksum file is empty!");
        });
    }

    @Test
    public void testRegenerateNugetChecksumInStorages()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_2 + "/org.carlspring.strongbox.checksum-one";

        deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertTrue(!new File(artifactPath, "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512").exists(),
                   "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (!jobName1.equals(jobName) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.1.0.nupkg.sha512"));
            resultList.add(new File(artifactPath,
                                        "/1.0/org.carlspring.strongbox.checksum-one.nuspec.sha512"));
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, null, null,
                         properties -> properties.put("forceRegeneration","false"));

        assertTrue(expectEvent(), "Failed to execute task!");
        
        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(),
                       "The checksum file doesn't exist!");
            assertTrue(f.length() > 0,
                       "The checksum file is empty!");
        });
    }

    private void createRepository(String storageId,
                                  String repositoryId,
                                  String policy)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        MutableRepository repository = nugetRepositoryFactory.createRepository(repositoryId);
        repository.setPolicy(policy);

        createRepository(storageId, repository);
    }

    private void createRepository(String storageId, MutableRepository repository)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repository.getId());
    }

    private void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new MutableStorage(storageId));
    }

    private void createStorage(MutableStorage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public static void cleanUp(Set<MutableRepository> repositoriesToClean)
            throws Exception
    {
        if (repositoriesToClean != null)
        {
            for (MutableRepository repository : repositoriesToClean)
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
            org.apache.commons.io.FileUtils.deleteDirectory(repositoryBaseDir);
        }
    }

    public void removeRepositories(Set<MutableRepository> repositoriesToClean)
            throws IOException, JAXBException
    {
        for (MutableRepository repository : repositoriesToClean)
        {
            configurationManagementService.removeRepository(repository.getStorage()
                                                                      .getId(), repository.getId());
        }
    }

    public static MutableRepository createRepositoryMock(String storageId,
                                                         String repositoryId)
    {
        // This is no the real storage, but has a matching ID.
        // We're mocking it, as the configurationManager is not available at the the static methods are invoked.
        MutableStorage storage = new MutableStorage(storageId);

        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setStorage(storage);

        return repository;
    }

}

