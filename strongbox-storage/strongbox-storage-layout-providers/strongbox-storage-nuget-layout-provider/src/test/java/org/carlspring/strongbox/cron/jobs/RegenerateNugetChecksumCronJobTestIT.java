package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.NugetLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.NugetRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NugetLayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class RegenerateNugetChecksumCronJobTestIT
        extends BaseCronJobWithNugetIndexingTestCase
{

    private static final String STORAGE1 = "storage-nuget";

    private static final String STORAGE2 = "nuget-checksum-test";

    private static final String REPOSITORY_RELEASES = "rnccj-releases";

    private static final String REPOSITORY_ALPHA = "rnccj-alpha";
    @Inject
    protected StorageManagementService storageManagementService;
    @Inject
    private PropertiesBooter propertiesBooter;
    @Inject
    private ConfigurationManagementService configurationManagementService;
    @Inject
    private RepositoryManagementService repositoryManagementService;
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

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_ALPHA));
        repositories.add(createRepositoryMock(STORAGE2, REPOSITORY_RELEASES));
        return repositories;
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
        File repositoryBaseDir = new File("target/strongbox-vault/storages/" + storageId + "/" + repositoryId);

        if (repositoryBaseDir.exists())
        {
            org.apache.commons.io.FileUtils.deleteDirectory(repositoryBaseDir);
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

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy());

        //Create released nuget package in the repository rnccj-releases (storage1)
        generateNugetArtifact(getRepositoryBasedir(STORAGE1, REPOSITORY_RELEASES),
                              "org.carlspring.strongbox.checksum-second", "1.0.0");

        createRepository(STORAGE1, REPOSITORY_ALPHA, RepositoryPolicyEnum.SNAPSHOT.getPolicy());

        //Create pre-released nuget package in the repository rnccj-alpha
        generateAlphaNugetArtifact(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                                   "org.carlspring.strongbox.checksum-one",
                                   "1.0.1");

        createStorage(STORAGE2);

        createRepository(STORAGE2, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy());

        //Create released nuget package in the repository rnccj-releases (storage2)
        generateNugetArtifact(getRepositoryBasedir(STORAGE2, REPOSITORY_RELEASES),
                              "org.carlspring.strongbox.checksum-one",
                              "1.0.0");
    }

    @AfterEach
    public void removeRepositories() throws IOException
    {
        removeRepositories(getRepositoriesToClean());
    }

    private String getRepositoryBasedir(String storageId,
                                        String repositoryId)
    {
        return Paths.get(propertiesBooter.getVaultDirectory() +
                         "/storages/" + storageId + "/" + repositoryId).toAbsolutePath().toString();
    }

    @Test
    public void testRegenerateNugetArtifactChecksum()
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        String artifactPath = getRepositoryBasedir(STORAGE1, REPOSITORY_RELEASES) +
                              "/org.carlspring.strongbox.checksum-second";

        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertFalse(
                new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512").exists(),
                "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (!StringUtils.equals(jobKey1, jobKey.toString()) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(artifactPath,
                                    "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512"));
            resultList.add(new File(artifactPath,
                                    "/1.0.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));
        });

        addCronJobConfig(jobKey, jobName, RegenerateChecksumCronJob.class, STORAGE1, REPOSITORY_RELEASES,
                         properties ->
                         {
                             properties.put("basePath", "org.carlspring.strongbox.checksum-second");
                             properties.put("forceRegeneration", "false");
                         });

        assertTrue(expectEvent(), "Failed to execute task!");

        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(),
                       "The checksum file " + f.toString() + " doesn't exist!");
            assertTrue(f.length() > 0,
                       "The checksum file is empty!");
        });
    }

    @Test
    public void testRegenerateNugetChecksumInRepository()
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        deleteIfExists(new File(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                                "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512"));
        deleteIfExists(new File(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                                "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertFalse(new File(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                             "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512").exists(),
                    "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1,
                                                                 statusExecuted) -> {
            if (!StringUtils.equals(jobKey1, jobKey.toString()) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                                    "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.1.0.1-alpha.nupkg.sha512"));
            resultList.add(new File(getRepositoryBasedir(STORAGE1, REPOSITORY_ALPHA),
                                    "/org.carlspring.strongbox.checksum-one/1.0.1-alpha/org.carlspring.strongbox.checksum-one.nuspec.sha512"));
        });
        addCronJobConfig(jobKey,
                         jobName,
                         RegenerateChecksumCronJob.class,
                         STORAGE1,
                         REPOSITORY_ALPHA,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue(expectEvent(), "Failed to execute task!");
        assertEquals(2, resultList.size());

        resultList.forEach(f -> {
            assertTrue(f.exists(), "The checksum file doesn't exist!");
            assertTrue(f.length() > 0, "The checksum file is empty!");
        });

    }

    @Test
    public void testRegenerateNugetChecksumInStorage()
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        String artifactPath =
                getRepositoryBasedir(STORAGE1, REPOSITORY_RELEASES) + "/org.carlspring.strongbox.checksum-second";

        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));

        assertFalse(
                new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512").exists(),
                "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1,
                                                                 statusExecuted) -> {
            if (!StringUtils.equals(jobKey1, jobKey.toString()) || !statusExecuted)
            {
                return;
            }
            resultList.add(
                    new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.1.0.0.nupkg.sha512"));
            resultList.add(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-second.nuspec.sha512"));
        });

        addCronJobConfig(jobKey, jobName, RegenerateChecksumCronJob.class, STORAGE1, null,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue(expectEvent(), "Failed to execute task!");

        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(), "The checksum file " + f.toString() + " doesn't exist!");
            assertTrue(f.length() > 0, "The checksum file is empty!");
        });
    }

    @Test
    public void testRegenerateNugetChecksumInStorages()
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        String artifactPath =
                getRepositoryBasedir(STORAGE2, REPOSITORY_RELEASES) + "/org.carlspring.strongbox.checksum-one";

        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-one.1.0.0.nupkg.sha512"));
        deleteIfExists(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-one.nuspec.sha512"));

        assertFalse(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-one.1.0.0.nupkg.sha512").exists(),
                    "The checksum file for artifact exist!");

        List<File> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1,
                                                                 statusExecuted) -> {
            if (!StringUtils.equals(jobKey1, jobKey.toString()) || !statusExecuted)
            {
                return;
            }
            resultList.add(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-one.1.0.0.nupkg.sha512"));
            resultList.add(new File(artifactPath, "/1.0.0/org.carlspring.strongbox.checksum-one.nuspec.sha512"));
        });

        addCronJobConfig(jobKey, jobName, RegenerateChecksumCronJob.class, null, null,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue(expectEvent(), "Failed to execute task!");

        assertEquals(2, resultList.size());
        resultList.forEach(f -> {
            assertTrue(f.exists(), "The checksum file " + f.toString() + " doesn't exist!");
            assertTrue(f.length() > 0, "The checksum file is empty!");
        });
    }

    private void createRepository(String storageId,
                                  String repositoryId,
                                  String policy)
            throws IOException,
                   RepositoryManagementStrategyException
    {
        MutableRepository repository = nugetRepositoryFactory.createRepository(repositoryId);
        repository.setPolicy(policy);

        createRepository(storageId, repository);
    }

    private void createRepository(String storageId,
                                  MutableRepository repository)
            throws IOException,
                   RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repository.getId());
    }

    private void createStorage(String storageId)
            throws IOException
    {
        createStorage(new MutableStorage(storageId));
    }

    private void createStorage(MutableStorage storage)
            throws IOException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.saveStorage(storage);
    }

    public void removeRepositories(Set<MutableRepository> repositoriesToClean) throws IOException
    {
        for (MutableRepository repository : repositoriesToClean)
        {
            configurationManagementService.removeRepository(repository.getStorage()
                                                                      .getId(), repository.getId());
        }
    }

}
