package org.carlspring.strongbox.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hazelcast.core.DistributedObject;
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
        bornCaches();

        final Set<Cache> caches = new HashSet<>();
        final Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();

        for (final DistributedObject distributedObject : distributedObjects)
        {
            if (distributedObject instanceof IMap)
            {
                final IMap<Object, Object> map = (IMap) distributedObject;
                final Cache cache = new HazelcastCache(map);
                caches.add(cache);
            }
        }
        return caches;
    }

    private Set<IMap> bornCaches()
    {
        final Set<IMap> caches = new LinkedHashSet<>();

        hazelcastInstance.getConfig().getMapConfigs().keySet().forEach(
                cacheName -> caches.add(hazelcastInstance.getMap(cacheName)));

        return caches;

    }
}
