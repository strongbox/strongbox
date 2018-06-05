package org.carlspring.strongbox.cron.jobs;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.junit.After;
import org.junit.Assume;
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

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildMavenIndexesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES_1 = "rmicj-releases";

    private static final String REPOSITORY_RELEASES_2 = "rmicj-releases-test";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_2);

    private static final File REPOSITORY_RELEASES_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

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
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @BeforeClass
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

    @Before
    public void isIndexingEnabled()
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());
    }

    @Before
    public void initialize()
            throws Exception
    {
        createStorage(STORAGE1);

        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);
        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);
        createRepository(STORAGE1, REPOSITORY_RELEASES_1, true);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE1, REPOSITORY_RELEASES_1);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_2);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_1);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                          "+g:org.carlspring.strongbox.indexes " +
                                                          "+a:strongbox-test-one " +
                                                          "+v:1.0 " +
                                                          "+p:jar",
                                                          MavenIndexerSearchProvider.ALIAS);

                try
                {
                    assertTrue(artifactSearchService.contains(request));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1,
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    SearchRequest request1 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar",
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-two " +
                                                               "+v:1.0 " +
                                                               "+p:jar",
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        assertTrue("Failed to execute task!", expectEvent());
    }

}
