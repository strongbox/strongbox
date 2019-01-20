package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroup;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;

import java.util.HashMap;
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
public class RepositoryArtifactIdGroupServiceImpl
        extends CommonCrudService<RepositoryArtifactIdGroup>
        implements RepositoryArtifactIdGroupService
{

    @Override
    public RepositoryArtifactIdGroup findOneOrCreate(String storageId,
                                                     String repositoryId,
                                                     String id)
    {
        Optional<RepositoryArtifactIdGroup> optional = tryFind(storageId, repositoryId, id);
        if (optional.isPresent())
        {
            return optional.get();
        }

        RepositoryArtifactIdGroup artifactGroup = new RepositoryArtifactIdGroup(storageId, repositoryId, id);

        try
        {
            return save(artifactGroup);
        }
        catch (ONeedRetryException ex)
        {
            optional = tryFind(storageId, repositoryId, id);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    private Optional<RepositoryArtifactIdGroup> tryFind(String storageId,
                                                        String repositoryId,
                                                        String id)
    {
        Map<String, String> params = new HashMap<>();
        params.put("storageId", storageId);
        params.put("repositoryId", repositoryId);
        params.put("id", id);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<RepositoryArtifactIdGroup> resultList = getDelegate().command(oQuery)
                                                                  .execute(params);
        return resultList.stream()
                         .findFirst();
    }
}
