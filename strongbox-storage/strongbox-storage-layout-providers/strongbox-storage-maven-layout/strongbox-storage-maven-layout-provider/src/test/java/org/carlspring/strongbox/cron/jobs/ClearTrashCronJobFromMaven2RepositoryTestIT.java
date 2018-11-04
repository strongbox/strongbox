package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ClearTrashCronJobFromMaven2RepositoryTestIT
        extends BaseCronJobWithMavenIndexingTestCase
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
    private static MutableRepository repository1;
    private static MutableRepository repository2;
    private static MutableRepository repository3;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        repository1 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_1);
        repository1.setAllowsForceDeletion(false);
        repository1.setTrashEnabled(true);
        repository1.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repository1);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");

        repository2 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_2);
        repository2.setAllowsForceDeletion(false);
        repository2.setTrashEnabled(true);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        createRepository(STORAGE0, repository2);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-two:1.0:jar");

        createStorage(new MutableStorage(STORAGE1));

        repository3 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_1);
        repository3.setAllowsForceDeletion(false);
        repository3.setTrashEnabled(true);
        repository3.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE1, repository3);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");
    }

    @AfterEach
    public void removeRepositories()
    {
        try
        {
            removeRepositories(getRepositoriesToClean());
        }
        catch (IOException | JAXBException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Test
    public void testRemoveTrashInRepository()
            throws Exception
    {
        File[] dirs = getDirs();

        assertNotNull(dirs, "There is no path to the repository trash!");
        assertEquals(0, dirs.length, "The repository trash isn't empty!");

        RepositoryPath path = repositoryPathResolver.resolve(new Repository(repository1), "org/carlspring/strongbox/clear/strongbox-test-one/1.0");
        RepositoryFiles.delete(path, false);

        dirs = getDirs();

        assertNotNull(dirs, "There is no path to the repository trash!");
        assertEquals(1, dirs.length, "The repository trash is empty!");

        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {

                File[] dirs1 = getDirs();

                assertNotNull(dirs1, "There is no path to the repository trash!");
                assertEquals(0, dirs1.length, "The repository trash isn't empty!");

                removeRepositories();
            }
        });

        addCronJobConfig(jobName, ClearRepositoryTrashCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    private File[] getDirs() 
    {
        RepositoryPath trashPath = null;
        try
        {
            trashPath = RepositoryFiles.trash(repositoryPathResolver.resolve(new Repository(repository1)));
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        final List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(trashPath))
        {
            for (Path path : directoryStream)
            {
                files.add(path.toFile());
            }
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        return files.toArray(new File[0]);
    }

    @Test
    public void testRemoveTrashAllRepositories()
            throws Exception
    {


        MutableConfiguration currentConfiguration = configurationManagementService.getMutableConfigurationClone();
        MutableConfiguration configurationBackup = configurationManagementService.getMutableConfigurationClone();

        try
        {
            removeNotMavenRepositories(currentConfiguration);

            final File basedirTrash1 = RepositoryFiles.trash(repositoryPathResolver.resolve(new Repository(repository2))).toFile();
            File[] dirs1 = basedirTrash1.listFiles();

            assertNotNull(dirs1, "There is no path to the repository trash!");
            assertEquals(0, dirs1.length, "The repository trash isn't empty!");

            LayoutProvider layoutProvider1 = layoutProviderRegistry.getProvider(repository2.getLayout());
            RepositoryPath path1 = repositoryPathResolver.resolve(new Repository(repository2), "org/carlspring/strongbox/clear/strongbox-test-two/1.0");
            RepositoryFiles.delete(path1, false);

            final File basedirTrash2 = RepositoryFiles.trash(repositoryPathResolver.resolve(new Repository(repository3))).toFile();
            File[] dirs2 = basedirTrash2.listFiles();

            assertNotNull(dirs2, "There is no path to the repository trash!");
            assertEquals(0, dirs2.length, "The repository trash isn't empty!");

            RepositoryPath path2 = repositoryPathResolver.resolve(new Repository(repository3), "org/carlspring/strongbox/clear/strongbox-test-one/1.0");
            RepositoryFiles.delete(path2, false);

            dirs1 = basedirTrash1.listFiles();
            dirs2 = basedirTrash1.listFiles();

            assertNotNull(dirs1, "There is no path to the repository trash!");
            assertEquals(1, dirs1.length, "The repository trash is empty!");
            assertNotNull(dirs2, "There is no path to the repository trash!");
            assertEquals(1, dirs2.length, "The repository trash is empty!");

            // Checking if job was executed
            final String jobName = expectedJobName;
            jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
            {
                File[] dirs11 = basedirTrash1.listFiles();
                File[] dirs22 = basedirTrash2.listFiles();

                assertNotNull(dirs11, "There is no path to the repository trash!");
                assertEquals(0, dirs11.length, "The repository trash isn't empty!");
                assertNotNull(dirs22, "There is no path to the repository trash!");
                assertEquals(0, dirs22.length, "The repository trash isn't empty!");

                removeRepositories();
            });

            addCronJobConfig(jobName, ClearRepositoryTrashCronJob.class, null, null);

            await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
        }
        finally
        {
            configurationManagementService.setConfiguration(configurationBackup);
        }
    }

    private void removeNotMavenRepositories(final MutableConfiguration currentConfiguration)
    {
        for (MutableStorage storage : currentConfiguration.getStorages().values())
        {
            Collection<MutableRepository> repositories = Lists.newArrayList(storage.getRepositories().values());
            for (MutableRepository repository : repositories)
            {
                if (Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
                {
                    continue;
                }
                storage.removeRepository(repository.getId());
            }
        }
        configurationManagementService.setConfiguration(currentConfiguration);
    }

}
