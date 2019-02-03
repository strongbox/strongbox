package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;
import org.carlspring.strongbox.util.BeanUtils;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.record.impl.ODocument;
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
                                                       Map<String, ? extends Object> properties)
    {
        Optional<T> optional = tryFind(type, properties);
        if (optional.isPresent())
        {
            return optional.get();
        }

        T artifactGroup = instantiateNew(type, properties);

        try
        {
            return save(artifactGroup);
        }
        catch (ONeedRetryException ex)
        {
            optional = tryFind(type, properties);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    private <T extends ArtifactGroup> T instantiateNew(Class<T> type,
                                                       Map<String, ? extends Object> properties)
    {
        T artifactGroup;
        try
        {
            artifactGroup = type.newInstance();
            BeanUtils.populate(artifactGroup, properties);
        }
        catch (Exception e)
        {
            throw new UndeclaredThrowableException(e);
        }
        return artifactGroup;
    }

    private <T extends ArtifactGroup> Optional<T> tryFind(Class<T> type,
                                                          Map<String, ? extends Object> properties)
    {
        String sQuery = buildQuery(properties, type);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<T> resultList = getDelegate().command(oQuery)
                                          .execute(properties);
        return resultList.stream()
                         .findFirst();
    }

}
