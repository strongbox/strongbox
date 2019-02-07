package org.carlspring.strongbox.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroup;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Service
@Transactional
public class RepositoryArtifactIdGroupServiceImpl
        extends AbstractArtifactGroupService<RepositoryArtifactIdGroup>
        implements RepositoryArtifactIdGroupService
{

    @Override
    public void addArtifactToGroup(RepositoryArtifactIdGroup artifactGroup,
                                   ArtifactEntry artifactEntry)
    {
        //TODO: move `ArtifactEntryServiceImpl.updateLastVersionTag` logic here.
    }

    public RepositoryArtifactIdGroup findOneOrCreate(String storageId,
                                                     String repositoryId,
                                                     String artifactId)
    {
        Optional<RepositoryArtifactIdGroup> optional = tryFind(storageId, repositoryId, artifactId);
        if (optional.isPresent())
        {
            return optional.get();
        }

        RepositoryArtifactIdGroup artifactGroup = create(storageId, repositoryId, artifactId);

        try
        {
            return save(artifactGroup);
        }
        catch (ONeedRetryException ex)
        {
            optional = tryFind(storageId, repositoryId, artifactId);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    protected RepositoryArtifactIdGroup create(String storageId,
                                               String repositoryId,
                                               String artifactId)
    {
        return new RepositoryArtifactIdGroup(storageId, repositoryId, artifactId);
    }

    protected Optional<RepositoryArtifactIdGroup> tryFind(String storageId,
                                                          String repositoryId,
                                                          String artifactId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("storageId", storageId);
        params.put("repositoryId", repositoryId);
        params.put("id", artifactId);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<RepositoryArtifactIdGroup> resultList = getDelegate().command(oQuery)
                                                                  .execute(params);
        return resultList.stream().findFirst();
    }

}
