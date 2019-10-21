package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RemoteRepositoryAlivenessCacheManagerTest
{

    static final String REMOTE_REPO_URL = "https://strongbox.github.io/";

    @Mock
    CacheManager cacheManager;
    @Mock
    Cache cache;
    RemoteRepositoryDto remoteRepository;
    RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    private static Stream<Arguments> repoAlivenessProvider()
    {
        return Stream.of(
                Arguments.of(RepoInCache.FOUND_IN_CACHE, RepoAliveness.ALIVE),
                Arguments.of(RepoInCache.NOT_FOUND_IN_CACHE, RepoAliveness.DEAD)
        );
    }

    @BeforeEach
    void setUp()
    {
        initMocks(this);
        when(cacheManager.getCache(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS)).thenReturn(cache);
        remoteRepository = new RemoteRepositoryDto();
        remoteRepository.setUrl(REMOTE_REPO_URL);
    }

    @Test
    void testCacheManagerHavingNoCacheForRepo()
    {
        // Given
        when(cacheManager.getCache(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS)).thenReturn(null);

        // When - Then
        assertThrows(NullPointerException.class, () -> new RemoteRepositoryAlivenessCacheManager(cacheManager));
    }

    @ParameterizedTest
    @MethodSource("repoAlivenessProvider")
    void testCachedValuesForRepo(RepoInCache repoFoundInCache,
                                 RepoAliveness expectedAlive)
    {
        // Given
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);
        when(cache.get(REMOTE_REPO_URL, Boolean.class)).thenReturn(repoFoundInCache.repoInCache);

        // When
        boolean repositoryIsAlive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        // Then
        assertThat(repositoryIsAlive).isEqualTo(expectedAlive.expectedRepoAliveness);
    }

    @Test
    void testNoCachedValueForRepo()
    {
        // Given
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);
        when(cache.get(REMOTE_REPO_URL, Boolean.class)).thenReturn(null);

        // When
        boolean repositoryIsAlive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        // Then
        assertThat(repositoryIsAlive).isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(RepoAliveness.class)
    void putRepository(RepoAliveness repoAliveness)
    {
        // Given
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);

        // When
        remoteRepositoryAlivenessCacheManager.put(remoteRepository, repoAliveness.expectedRepoAliveness);

        // Then
        verify(cache, times(1)).put(REMOTE_REPO_URL, repoAliveness.expectedRepoAliveness);
    }

    @Test
    void destroyRepositories()
            throws Exception
    {
        // Given
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);

        // When
        remoteRepositoryAlivenessCacheManager.destroy();

        // Then
        verify(cache, times(1)).clear();
    }

    enum RepoInCache
    {
        FOUND_IN_CACHE(true),
        NOT_FOUND_IN_CACHE(false);

        private final boolean repoInCache;

        RepoInCache(boolean repoInCache)
        {
            this.repoInCache = repoInCache;
        }
    }

    enum RepoAliveness
    {
        ALIVE(true),
        DEAD(false);

        private final boolean expectedRepoAliveness;

        RepoAliveness(boolean expectedRepoAliveness)
        {
            this.expectedRepoAliveness = expectedRepoAliveness;
        }
    }
}
