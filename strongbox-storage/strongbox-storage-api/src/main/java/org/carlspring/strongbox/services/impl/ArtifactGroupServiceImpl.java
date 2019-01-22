package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Przemyslaw Fusik
 */
@Service
@Transactional
public class ArtifactGroupServiceImpl
        extends CommonCrudService<ArtifactGroup>
        implements ArtifactGroupService
{

    private static final String ARTIFACT_GROUP_MAP_LOCKS = "artifact-group-map-locks";

    @Inject
    private HazelcastInstance hazelcastInstance;

    @Override
    public Class<ArtifactGroup> getEntityClass()
    {
        return ArtifactGroup.class;
    }

    @Override
    public <T extends ArtifactGroup> T findOneOrCreate(Class<T> type,
                                                       String name)
    {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("type", type.getName());

        String sQuery = buildQuery(params);

        OSQLSynchQuery<Long> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        // make sure there are no duplicates per `params`
        lock(params);

        T result;
        try
        {
            List<T> resultList = getDelegate().command(oQuery)
                                              .execute(params);

            result = resultList.stream()
                               .findFirst()
                               .orElseGet(() -> {
                                   T artifactGroup;
                                   try
                                   {
                                       artifactGroup = type.newInstance();
                                   }
                                   catch (Exception e)
                                   {
                                       throw new UndeclaredThrowableException(e);
                                   }
                                   artifactGroup.setName(name);
                                   return getDelegate().detach(save(artifactGroup));
                               });
        }
        finally
        {
            unlock(params);
        }

        return result;
    }

    /**
     * @see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Distributed_Data_Structures/Lock.html#page_ILock+vs.+IMap.lock">ILock vs. IMap.lock</a>
     */
    public void lock(Map<String, String> params)
    {
        final IMap mapLocks = hazelcastInstance.getMap(ARTIFACT_GROUP_MAP_LOCKS);
        // DEV NOTE: map may be empty for locking
        // we don't have to put values to the map
        mapLocks.lock(params);
    }

    public void unlock(Map<String, String> params)
    {
        final IMap mapLocks = hazelcastInstance.getMap(ARTIFACT_GROUP_MAP_LOCKS);
        // The IMap-based locks are auto-destructed.
        mapLocks.unlock(params);
    }

}
