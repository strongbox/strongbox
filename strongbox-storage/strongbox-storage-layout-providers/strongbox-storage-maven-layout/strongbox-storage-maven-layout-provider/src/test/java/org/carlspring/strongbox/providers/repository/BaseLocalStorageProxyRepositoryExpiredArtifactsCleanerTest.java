package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryExpiredArtifactsCleaner;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{
    protected static final String STORAGE_ID = "storage-common-proxies";

    protected static final String REMOTE_URL = "http://central.maven.org/maven2/";

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

    @AfterEach
    public void cleanup()
    {
        artifactEntryService.delete(
                artifactEntryService.findArtifactList(STORAGE_ID,
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

    protected void synchronizeCleanupExpiredArtifacts(boolean isAlive,
                                                      ArtifactEntry artifactEntry)
            throws IOException, SearchException
    {
        synchronized (BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest.class)
        {
            Mockito.when(remoteRepositoryAlivenessCacheManager.isAlive(
                    argThat(argument -> argument != null && REMOTE_URL.equals(argument.getUrl()))))
                   .thenReturn(isAlive);

            localStorageProxyRepositoryExpiredArtifactsCleaner.cleanup(5, artifactEntry.getSizeInBytes() - 1);
        }
    }

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
