package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.data.service.SynchronizedCommonCrudService;

import java.util.HashMap;
import java.util.List;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.springframework.stereotype.Component;

@Component
public class ArtifactCoordinatesService
        extends SynchronizedCommonCrudService<AbstractArtifactCoordinates>
{

    @Override
    protected ORID findId(AbstractArtifactCoordinates entity)
    {
        String sQuery = String.format("SELECT FROM INDEX:idx_artifact_coordinates WHERE key = :path");

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        HashMap<String, Object> params = new HashMap<>();
        params.put("path", entity.toPath());

        return ((List<ODocument>) getDelegate().command(oQuery)
                                               .execute(params)).stream()
                                                                .limit(1)
                                                                .map(d -> ((ODocument) d.field(
                                                                        "rid")).getIdentity())
                                                                .findFirst()
                                                                .orElse(null);
    }

    @Override
    public Class<AbstractArtifactCoordinates> getEntityClass()
    {
        return AbstractArtifactCoordinates.class;
    }

    @Override
    protected String getLockKey(AbstractArtifactCoordinates entity)
    {
        return entity.getPath();
    }
}
