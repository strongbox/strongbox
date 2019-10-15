package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import java.util.Objects;
import java.util.stream.Stream;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@SpringBootTest
@ActiveProfiles({ "test",
                  "RemoteRepositoryAlivenessCacheManagerTestConfig" })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(SAME_THREAD)
public class RemoteRepositoryAlivenessCacheManagerTest
{

    @Inject
    private CacheManager cacheManager;

    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    private RemoteRepository remoteRepository;

    private final static String REMOTE_REPOSITORY_URL = "http://mocked-remote-repository-url";

    @BeforeEach
    void setUp()
    {
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);

        remoteRepository = Mockito.mock(RemoteRepository.class);
        Mockito.when(remoteRepository.getUrl()).thenReturn(REMOTE_REPOSITORY_URL);
    }

    @Test
    public void isAliveShouldReturnTrueWhenRemoteRepositoryNotCached()
    {
        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertThat(alive).isTrue();
    }

    @Test
    public void isAliveShouldReturnTrueWhenRemoteRepositoryCachedValueIsTrue()
    {
        initializeCache(true);

        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertThat(alive).isTrue();
    }

    @Test
    public void isAliveShouldReturnFalseWhenRemoteRepositoryCachedValueIsFalse()
    {
        initializeCache(false);
        remoteRepositoryAlivenessCacheManager.put(remoteRepository, false);

        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertThat(alive).isFalse();
    }

    @Test
    public void isAliveShouldReturnTrueWhenRemoteRepositoryCachedValueIsExpired()
    {
        // TODO: Figure out how to forcibly expire a cached item.
    }

    @ParameterizedTest
    @MethodSource("provideCacheValues")
    public void putShouldChangeCachedValueImmediately(Boolean initialCacheValue,
                                                      boolean newCacheValue)
    {
        initializeCache(initialCacheValue);

        remoteRepositoryAlivenessCacheManager.put(remoteRepository, newCacheValue);
        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertThat(alive).isEqualTo(newCacheValue);
    }

    private static Stream<Arguments> provideCacheValues()
    {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of(null, true),
                Arguments.of(false, false),
                Arguments.of(true, true)
        );
    }

    private void initializeCache(Boolean initialValue)
    {
        Cache cache = cacheManager.getCache(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS);
        Objects.requireNonNull(cache, "remoteRepositoryAliveness cache configuration was not provided");

        cache.put(REMOTE_REPOSITORY_URL, initialValue);
    }

    @Profile("RemoteRepositoryAlivenessCacheManagerTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class MockedRestArtifactResolverTestConfig
    {

        @Bean
        public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
        {
            return new HazelcastCacheManager(hazelcastInstance);
        }

    }

}
