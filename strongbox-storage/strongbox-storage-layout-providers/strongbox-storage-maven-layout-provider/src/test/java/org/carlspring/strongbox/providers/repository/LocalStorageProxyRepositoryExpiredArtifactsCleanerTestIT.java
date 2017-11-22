package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryExpiredArtifactsCleaner;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.plexus.util.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
public class LocalStorageProxyRepositoryExpiredArtifactsCleanerTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private LocalStorageProxyRepositoryExpiredArtifactsCleaner localStorageProxyRepositoryExpiredArtifactsCleaner;

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
    public void expiredArtifactsCleanerShouldCleanupDatabaseAndStorage()
            throws Exception
    {
        String storageId = "storage-common-proxies";
        String repositoryId = "maven-central";
        String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        Optional<ArtifactEntry> artifactEntryOptional = artifactEntryService.findOneAritifact(storageId, repositoryId,
                                                                                              path);
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        try (final InputStream ignored = proxyRepositoryProvider.getInputStream(storageId, repositoryId, path))
        {
        }

        artifactEntryOptional = artifactEntryService.findOneAritifact(storageId, repositoryId, path);
        ArtifactEntry artifactEntry = artifactEntryOptional.orElse(null);
        assertThat(artifactEntry, CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUpdated(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUsed(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), Matchers.greaterThan(0l));

        artifactEntry.setLastUsed(
                DateUtils.addDays(artifactEntry.getLastUsed(), -10));
        Long sizeInBytes = artifactEntry.getSizeInBytes();

        artifactEntryService.save(artifactEntry);

        localStorageProxyRepositoryExpiredArtifactsCleaner.cleanup(5, sizeInBytes - 1);
        artifactEntryOptional = artifactEntryService.findOneAritifact(storageId, repositoryId, path);
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        Optional<ArtifactEntry> artifactMetadataOptional = artifactEntryService.findOneAritifact(storageId,
                                                                                                 repositoryId,
                                                                                                 StringUtils.replace(
                                                                                                         path,
                                                                                                         "1.6/properties-injector-1.6.jar",
                                                                                                         "maven-metadata.xml"));

        // we haven't touched the last used of the maven-metadata ;)
        assertThat(artifactMetadataOptional, CoreMatchers.not(CoreMatchers.equalTo(Optional.empty())));

        final Storage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
        final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        assertFalse(layoutProvider.containsPath(repository, path));
        assertTrue(layoutProvider.containsPath(repository, StringUtils.replace(path, "1.6/properties-injector-1.6.jar",
                                                                               "maven-metadata.xml")));
    }

}
