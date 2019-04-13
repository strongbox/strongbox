package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryExpiredArtifactsCleaner;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{
    protected static final String STORAGE_ID = "storage-common-proxies";

    @Inject
    protected ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    protected ArtifactEntryService artifactEntryService;

    @Inject
    protected LocalStorageProxyRepositoryExpiredArtifactsCleaner localStorageProxyRepositoryExpiredArtifactsCleaner;

    @Inject
    @Named("mockedRemoteRepositoryAlivenessCacheManager")
    protected RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @AfterEach
    public void cleanup()
    {
        artifactEntryService.delete(artifactEntryService.findArtifactList(STORAGE_ID,
                                                                          getRepositoryId(),
                                                                          ImmutableMap.of("groupId", getGroupId(),
                                                                                          "artifactId", getArtifactId(),
                                                                                          "version", getVersion()),
                                                                          true));
    }

    protected abstract String getRepositoryId();

    protected abstract String getPath();

    protected String getGroupId()
    {
        return "org.carlspring.maven";
    }

    protected String getArtifactId()
    {
        return "maven-commons";
    }

    protected abstract String getVersion();

    protected ArtifactEntry downloadAndSaveArtifactEntry()
            throws Exception
    {
        Optional<ArtifactEntry> artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                                                 getRepositoryId(),
                                                                                                                 getPath()));
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        RepositoryPath repositoryPath = proxyRepositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE_ID,
                                                                                                         getRepositoryId(),
                                                                                                         getPath()));
        try (final InputStream ignored = proxyRepositoryProvider.getInputStream(repositoryPath))
        {
        }

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         getRepositoryId(),
                                                                                         getPath()));
        ArtifactEntry artifactEntry = artifactEntryOptional.orElse(null);
        assertThat(artifactEntry, CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUpdated(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getLastUsed(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), CoreMatchers.notNullValue());
        assertThat(artifactEntry.getSizeInBytes(), Matchers.greaterThan(0l));

        artifactEntry.setLastUsed(DateUtils.addDays(artifactEntry.getLastUsed(), -10));

        return artifactEntryService.save(artifactEntry);
    }

}
