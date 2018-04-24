package org.carlspring.strongbox.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spring.cache.HazelcastCache;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

/**
 * @author Przemyslaw Fusik
 */
public class HazelcastTransactionSupportingCacheManager
        extends AbstractTransactionSupportingCacheManager
{

    private HazelcastInstance hazelcastInstance;

    public HazelcastTransactionSupportingCacheManager(HazelcastInstance hazelcastInstance)
    {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    protected Collection<? extends Cache> loadCaches()
    {
        final Set<Cache> caches = new HashSet<>();

        for (final IMap distributedCache : getDistributedCaches())
        {
            final Cache cache = new HazelcastCache(distributedCache);
            caches.add(cache);
        }
        return caches;
    }

    private Set<IMap> getDistributedCaches()
    {
        final Set<IMap> caches = new LinkedHashSet<>();

        hazelcastInstance.getConfig()
                         .getMapConfigs()
                         .keySet().forEach(cacheName -> caches.add(hazelcastInstance.getMap(cacheName)));

        return caches;

    }
}
