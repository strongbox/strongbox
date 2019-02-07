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
    protected RepositoryArtifactIdGroup create(ArtifactEntry artifactEntry)
    {
        return new RepositoryArtifactIdGroup(artifactEntry.getStorageId(), artifactEntry.getRepositoryId(),
                artifactEntry.getArtifactCoordinates().getId());
    }

    @Override
    protected Optional<RepositoryArtifactIdGroup> tryFind(ArtifactEntry artifactEntry)
    {
        Map<String, String> params = new HashMap<>();
        params.put("storageId", artifactEntry.getStorageId());
        params.put("repositoryId", artifactEntry.getRepositoryId());
        params.put("id", artifactEntry.getArtifactCoordinates().getId());

        String sQuery = buildQuery(params);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<RepositoryArtifactIdGroup> resultList = getDelegate().command(oQuery)
                                                                  .execute(params);
        return resultList.stream().findFirst();
    }

}
