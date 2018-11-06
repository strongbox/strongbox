package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Martin Todorov
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
public class DownloadRemoteMavenIndexCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES = "drmicj-releases";

    private static final String REPOSITORY_PROXIED_RELEASES = "drmicj-proxied-releases";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_RELEASES);

    @Inject
    private ArtifactSearchService artifactSearchService;

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXIED_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createRepository(STORAGE0, REPOSITORY_RELEASES, true);

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXIED_RELEASES,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar");

        reIndex(STORAGE0, REPOSITORY_RELEASES, "/");
        reIndex(STORAGE0, REPOSITORY_PROXIED_RELEASES, "/");

        packIndex(STORAGE0, REPOSITORY_RELEASES);

        // Requests against the local index of the hosted repository from which we're later on proxying:
        // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   REPOSITORY_RELEASES,
                                                   "+g:org.carlspring.strongbox.indexes.download " +
                                                   "+a:strongbox-test-one " +
                                                   "+v:1.0 " +
                                                   "+p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);
        // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   REPOSITORY_RELEASES,
                                                   "+g:org.carlspring.strongbox.indexes.download " +
                                                   "+a:strongbox-test-two " +
                                                   "+v:1.0 " +
                                                   "+p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        // Check that the artifacts exist in the hosted repository's local index
        assertTrue(artifactSearchService.contains(request1),
                   "Failed to find any results for " + request1.getQuery() + " in the hosted repository!");

        System.out.println(request1.getQuery() + " found matches!");

        assertTrue(artifactSearchService.contains(request2),
                   "Failed to find any results for " + request2.getQuery() + " in the hosted repository!");

        System.out.println(request2.getQuery() + " found matches!");
    }

    @Test
    public void testDownloadRemoteIndexAndExecuteSearch()
            throws Exception
    {
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                // Requests against the remote index on the proxied repository:
                // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
                SearchRequest request3 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_PROXIED_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes.download " +
                                                           "+a:strongbox-test-one " +
                                                           "+v:1.0 " +
                                                           "+p:jar",
                                                           MavenIndexerSearchProvider.ALIAS);
                request3.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
                SearchRequest request4 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_PROXIED_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes.download " +
                                                           "+a:strongbox-test-two " +
                                                           "+v:1.0 " +
                                                           "+p:jar",
                                                           MavenIndexerSearchProvider.ALIAS);
                request4.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                try
                {
                    // Check that the artifacts exist in the proxied repository's remote index
                    assertTrue(artifactSearchService.contains(request3),
                               "Failed to find any results for " + request3.getQuery() + " in the remote index!");

                    System.out.println(request3.getQuery() + " found matches!");

                    assertTrue(artifactSearchService.contains(request4),
                               "Failed to find any results for " + request4.getQuery() + " in the remote index!");

                    System.out.println(request4.getQuery() + " found matches!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, DownloadRemoteMavenIndexCronJob.class, STORAGE0,
                         REPOSITORY_PROXIED_RELEASES);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
