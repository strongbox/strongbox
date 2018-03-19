package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryTrashPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
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
    private static Repository repository1;
    private static Repository repository2;
    private static Repository repository3;
    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(final Description description)
        {
            expectedJobName = description.getMethodName();
        }
    };
    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryTrashPathResolver repositoryTrashPathResolver;

    @Inject
    private ConfigurationManager configurationManager;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1));

        return repositories;
    }

    @Before
    public void initialize()
            throws Exception
    {
        MavenRepositoryConfiguration mavenRepositoryConfiguration = new MavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        repository1 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_RELEASES_1);
        repository1.setAllowsForceDeletion(false);
        repository1.setTrashEnabled(true);
        repository1.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repository1);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");

        repository2 = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_RELEASES_2);
        repository2.setAllowsForceDeletion(false);
        repository2.setTrashEnabled(true);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        createRepository(repository2);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-two:1.0:jar");

        createStorage(new Storage(STORAGE1));

        repository3 = mavenRepositoryFactory.createRepository(STORAGE1, REPOSITORY_RELEASES_1);
        repository3.setAllowsForceDeletion(false);
        repository3.setTrashEnabled(true);
        repository3.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repository3);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.clear:strongbox-test-one:1.0:jar");
    }

    public void removeRepositories()
    {
        try
        {
            removeRepositories(getRepositoriesToClean());
        }
        catch (IOException | JAXBException e)
        {
            throw Throwables.propagate(e);
        }
    }

    @Test
    public void testRemoveTrashInRepository()
            throws Exception
    {
        File[] dirs = getDirs();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash isn't empty!", 0, dirs.length);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository1.getLayout());
        String path = "org/carlspring/strongbox/clear/strongbox-test-one/1.0";
        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES_1, path, false);

        dirs = getDirs();

        assertTrue("There is no path to the repository trash!", dirs != null);
        assertEquals("The repository trash is empty!", 1, dirs.length);

        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {

                File[] dirs1 = getDirs();

                assertTrue("There is no path to the repository trash!", dirs1 != null);
                assertEquals("The repository trash isn't empty!", 0, dirs1.length);

                removeRepositories();
            }
        });

        addCronJobConfig(jobName, ClearRepositoryTrashCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        assertTrue("Failed to execute task!", expectEvent());
    }

    private File[] getDirs()
    {
        final RepositoryPath trashPath = repositoryTrashPathResolver.resolve(repository1);
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
            throw Throwables.propagate(e);
        }
        return files.toArray(new File[0]);
    }

    @Test
    public void testRemoveTrashAllRepositories()
            throws Exception
    {

        Configuration currentConfiguration = getConfiguration();
        Configuration configurationBackup = getConfiguration();

        try
        {
            removeNotMavenRepositories(currentConfiguration);

            final File basedirTrash1 = repositoryTrashPathResolver.resolve(repository2).toFile();
            File[] dirs1 = basedirTrash1.listFiles();

            assertTrue("There is no path to the repository trash!", dirs1 != null);
            assertEquals("The repository trash isn't empty!", 0, dirs1.length);

            LayoutProvider layoutProvider1 = layoutProviderRegistry.getProvider(repository2.getLayout());
            String path1 = "org/carlspring/strongbox/clear/strongbox-test-two/1.0";
            layoutProvider1.delete(STORAGE0, REPOSITORY_RELEASES_2, path1, false);

            final File basedirTrash2 = repositoryTrashPathResolver.resolve(repository3).toFile();
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

            // Checking if job was executed
            String jobName = expectedJobName;
            jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
            {
                File[] dirs11 = basedirTrash1.listFiles();
                File[] dirs22 = basedirTrash2.listFiles();

                assertTrue("There is no path to the repository trash!", dirs11 != null);
                assertEquals("The repository trash isn't empty!", 0, dirs11.length);
                assertTrue("There is no path to the repository trash!", dirs22 != null);
                assertEquals("The repository trash isn't empty!", 0, dirs22.length);

                removeRepositories();
            });

            addCronJobConfig(jobName, ClearRepositoryTrashCronJob.class, null, null);

            assertTrue("Failed to execute task!", expectEvent());
        }
        finally
        {
            configurationBackup.setUuid(null);
            configurationBackup.setObjectId(null);
            configurationManagementService.save(configurationBackup);
        }
    }

    private void removeNotMavenRepositories(final Configuration currentConfiguration)
    {
        for (Storage storage : currentConfiguration.getStorages().values())
        {
            Collection<Repository> repositories = Lists.newArrayList(storage.getRepositories().values());
            for (Repository repository : repositories)
            {
                if (Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
                {
                    continue;
                }
                storage.removeRepository(repository.getId());
            }
        }
        configurationManagementService.save(currentConfiguration);
    }

}
