package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.inject.Inject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.orientechnologies.orient.core.id.ORID;

public abstract class SynchronizedCommonCrudService<T extends GenericEntity>
        extends CommonCrudService<T>
{

    private final String locksMapName = getClass().getName() + "-entity-save-lock";

    @Inject
    private HazelcastInstance hazelcastInstance;

    @Override
    protected <S extends T> S cascadeEntitySave(final T entity)
    {
        final String lockKey = getLockKey(entity);
        final IMap lock = acquireLock(lockKey);
        try
        {
            return super.cascadeEntitySave(entity);
        }
        finally
        {
            lock.unlock(lockKey);
        }
    }

    @Override
    protected boolean identifyEntity(T entity)
    {
        if (super.identifyEntity(entity))
        {
            return true;
        }

        ORID objectId;

        final String lockKey = getLockKey(entity);
        final IMap lock = acquireLock(lockKey);
        try
        {
            objectId = findId(entity);
        }
        finally
        {
            lock.unlock(lockKey);
        }

        if (objectId == null)
        {
            return false;
        }

        entity.setObjectId(objectId.toString());

        return true;
    }


    /**
     * @see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Distributed_Data_Structures/Lock.html#page_ILock+vs.+IMap.lock">ILock vs. IMap.lock</a>
     */
    private IMap acquireLock(final String lockKey)
    {
        final IMap mapLocks = hazelcastInstance.getMap(locksMapName);
        // DEV NOTE: map may be empty for locking
        // we don't have to put values to the map
        mapLocks.lock(lockKey);
        return mapLocks;
    }

    protected abstract String getLockKey(T entity);

    protected abstract ORID findId(T entity);
}
