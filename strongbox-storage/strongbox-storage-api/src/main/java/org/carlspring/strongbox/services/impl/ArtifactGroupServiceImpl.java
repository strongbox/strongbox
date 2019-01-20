package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;

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

    @Override
    public Class<ArtifactGroup> getEntityClass()
    {
        return ArtifactGroup.class;
    }

    @Override
    public <T extends ArtifactGroup> T findOneOrCreate(Class<T> type,
                                                       String name)
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("type", type.getName());

        String sQuery = buildQuery(params);

        OSQLSynchQuery<Long> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<T> resultList = getDelegate().command(oQuery)
                                          .execute(params);

        return resultList.stream()
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

}
