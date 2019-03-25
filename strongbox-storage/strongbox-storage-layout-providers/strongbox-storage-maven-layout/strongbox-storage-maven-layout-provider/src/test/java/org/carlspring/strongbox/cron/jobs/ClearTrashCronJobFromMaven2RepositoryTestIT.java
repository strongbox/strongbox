package org.carlspring.strongbox.cron.jobs;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class ClearTrashCronJobFromMaven2RepositoryTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES_1 = "crtcj-releases";

    private static final String REPOSITORY_RELEASES_2 = "crtcj-releases-test";

    private static MutableRepository repository1;

    private static MutableRepository repository2;

    private static MutableRepository repository3;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    private static Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositories());
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

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");

        repository2 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_2);
        repository2.setAllowsForceDeletion(false);
        repository2.setTrashEnabled(true);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repository2);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-two:1.0:jar");

        createStorage(new MutableStorage(STORAGE1));

        repository3 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_1);
        repository3.setAllowsForceDeletion(false);
        repository3.setTrashEnabled(true);
        repository3.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE1, repository3);

        generateArtifact(getRepositoryBasedir(STORAGE1, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");
    }

    @Test
    public void testRemoveTrashInRepository()
            throws Exception
    {
        File[] dirs = getDirs();

        assertNotNull(dirs, "There is no path to the repository trash!");
        assertEquals(0, dirs.length, "The repository trash isn't empty!");

        RepositoryPath path = repositoryPathResolver.resolve(new ImmutableRepository(repository1),
                                                             "org/carlspring/strongbox/clear/strongbox-test-one/1.0");
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
            trashPath = RepositoryFiles.trash(repositoryPathResolver.resolve(new ImmutableRepository(repository1)));
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

}
