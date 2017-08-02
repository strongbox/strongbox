package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RemoteRepositoryAlivenessCacheManager
        implements DisposableBean
{

    private final Cache cache;

    @Inject
    RemoteRepositoryAlivenessCacheManager(CacheManager cacheManager)
    {
        cache = cacheManager.getCache("remoteRepositoryAliveness");
        Objects.requireNonNull(cache, "remoteRepositoryAliveness cache configuration was not provided");
    }

    public boolean isAlive(RemoteRepository remoteRepository)
    {
        return BooleanUtils.isTrue(cache.get(remoteRepository, Boolean.class));
    }

    public boolean wasPut(RemoteRepository remoteRepository)
    {
        return cache.get(remoteRepository, Boolean.class) != null;
    }

    public void put(RemoteRepository remoteRepository,
                    boolean aliveness)
    {
        cache.put(remoteRepository, Boolean.valueOf(aliveness));
    }

    @Override
    public void destroy()
            throws Exception
    {
        cache.clear();
    }
}
