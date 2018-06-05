package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.base.Throwables;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.plexus.util.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CleanupExpiredArtifactsFromProxyRepositoriesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
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
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Before
    @After
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/properties-injector");

        artifactEntryService.deleteAll();
    }

    @Test
    public void expiredArtifactsCleanupCronJobShouldCleanupDatabaseAndStorage()
            throws Exception
    {
        final String storageId = "storage-common-proxies";
        final String repositoryId = "maven-central";
        final String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        Optional<ArtifactEntry> artifactEntryOptional = artifactEntryService.findOneArtifact(storageId, repositoryId,
                                                                                             path);
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        Path repositoryPath = proxyRepositoryProvider.fetchPath(repositoryPathResolver.resolve(storageId, repositoryId,
                                                                                               path));
        try (final InputStream ignored = proxyRepositoryProvider.getInputStream(repositoryPath))
        {
        }

        artifactEntryOptional = artifactEntryService.findOneArtifact(storageId, repositoryId, path);
        ArtifactEntry artifactEntry = artifactEntryOptional.orElse(null);
        assertThat(artifactEntry, CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUpdated(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUsed(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), Matchers.greaterThan(0l));

        artifactEntry.setLastUsed(
                DateUtils.addDays(artifactEntry.getLastUsed(), -10));
        final Long sizeInBytes = artifactEntry.getSizeInBytes();

        artifactEntryService.save(artifactEntry);

        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                Optional<ArtifactEntry> optionalArtifactEntryFromDb = artifactEntryService.findOneArtifact(storageId,
                                                                                                           repositoryId,
                                                                                                           path);
                assertThat(optionalArtifactEntryFromDb, CoreMatchers.equalTo(Optional.empty()));

                final Storage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
                final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
                final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

                try
                {
                    assertFalse(layoutProvider.containsPath(repositoryPathResolver.resolve(repository, path)));
                    assertTrue(layoutProvider.containsPath(repositoryPathResolver.resolve(repository,
                                                                                          StringUtils.replace(path,
                                                                                                              "1.6/properties-injector-1.6.jar",
                                                                                                              "maven-metadata.xml"))));
                }
                catch (IOException e)
                {
                    throw Throwables.propagate(e);
                }

            }
        });

        addCronJobConfig(jobName, CleanupExpiredArtifactsFromProxyRepositoriesCronJob.class, storageId, repositoryId,
                         properties ->
                         {
                             properties.put("lastAccessedTimeInDays", "5");
                             properties.put("minSizeInBytes", Long.valueOf(sizeInBytes - 1).toString());
                         });

        assertTrue("Failed to execute task!", expectEvent());
    }

}
