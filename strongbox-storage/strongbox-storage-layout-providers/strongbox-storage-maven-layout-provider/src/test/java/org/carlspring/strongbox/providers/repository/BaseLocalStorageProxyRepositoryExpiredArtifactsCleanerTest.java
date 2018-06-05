package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryExpiredArtifactsCleaner;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
public class BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected String storageId = "storage-common-proxies";

    protected String repositoryId = "maven-central";

    protected String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

    @Inject
    protected ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    protected ArtifactEntryService artifactEntryService;

    @Inject
    protected LocalStorageProxyRepositoryExpiredArtifactsCleaner localStorageProxyRepositoryExpiredArtifactsCleaner;

    @Inject
    protected RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Before
    @After
    public void makeSureRemoteRepositoryIsRecognizedAsAlive()
            throws Exception
    {
        Mockito.when(remoteRepositoryAlivenessCacheManager.isAlive(any(RemoteRepository.class))).thenReturn(true);
    }

    @Before
    @After
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/properties-injector");

        artifactEntryService.deleteAll();
    }

    protected ArtifactEntry downloadAndSaveArtifactEntry()
            throws Exception
    {
        Optional<ArtifactEntry> artifactEntryOptional = artifactEntryService.findOneArtifact(storageId, repositoryId,
                                                                                             path);
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        RepositoryPath repositoryPath = proxyRepositoryProvider.fetchPath(repositoryPathResolver.resolve(storageId, repositoryId,
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

        return artifactEntryService.save(artifactEntry);
    }
}
