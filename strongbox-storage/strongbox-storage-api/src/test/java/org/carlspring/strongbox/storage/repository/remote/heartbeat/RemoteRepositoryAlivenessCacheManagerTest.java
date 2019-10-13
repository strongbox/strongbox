package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.config.*;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.NullLayoutConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

import javax.inject.Inject;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({ "test",
                  "RemoteRepositoryAlivenessCacheManagerTestConfig" })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class RemoteRepositoryAlivenessCacheManagerTest
{

    @Inject
    private CacheManager cacheManager;

    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    private final static String REMOTE_REPOSITORY_URL = "http://mocked-remote-repository-url";

    @BeforeEach
    void setUp()
    {
        remoteRepositoryAlivenessCacheManager = new RemoteRepositoryAlivenessCacheManager(cacheManager);
    }

    @Test
    public void isAliveShouldReturnTrueWhenRemoteRepositoryNotCached()
    {
        RemoteRepository remoteRepository = createMockedRemoteRepository();

        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertTrue(alive);
    }

    @Test
    public void isAliveShouldReturnTrueWhenRemoteRepositoryCachedValueIsTrue()
    {
        RemoteRepository remoteRepository = createMockedRemoteRepository();

        initializeCache(true);

        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertTrue(alive);
    }

    @Test
    public void isAliveShouldReturnFalseWhenRemoteRepositoryCachedValueIsFalse()
    {
        RemoteRepository remoteRepository = createMockedRemoteRepository();

        initializeCache(false);
        remoteRepositoryAlivenessCacheManager.put(remoteRepository, false);

        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertFalse(alive);
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
        RemoteRepository remoteRepository = createMockedRemoteRepository();

        initializeCache(initialCacheValue);

        remoteRepositoryAlivenessCacheManager.put(remoteRepository, newCacheValue);
        boolean alive = remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);

        assertEquals(newCacheValue, alive);
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

    private RemoteRepository createMockedRemoteRepository()
    {
        RemoteRepository remoteRepository = Mockito.mock(RemoteRepository.class);
        Mockito.when(remoteRepository.getUrl()).thenReturn(REMOTE_REPOSITORY_URL);

        return remoteRepository;
    }

    private void initializeCache(Boolean initialValue)
    {
        Cache cache = cacheManager.getCache(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS);
        Objects.requireNonNull(cache, "remoteRepositoryAliveness cache configuration was not provided");

        cache.put(REMOTE_REPOSITORY_URL, initialValue);
    }

    @Profile("RemoteRepositoryAlivenessCacheManagerTestConfig")
    @Import({ HazelcastConfiguration.class,
              TestingCoreConfig.class,
              CommonConfig.class,
              ClientConfig.class,
              DataServiceConfig.class,
              EventsConfig.class,
              StorageCoreConfig.class,
              StorageApiConfig.class,
              NullLayoutConfiguration.class })
    @Configuration
    public static class MockedRestArtifactResolverTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdRracmt()
        {
            return new HazelcastInstanceId("RemoteRepositoryAlivenessCacheManagerTest-hazelcast-instance");
        }

    }

}
