package org.carlspring.strongbox.services;

import java.util.HashMap;
import java.util.List;

import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public abstract class AbstractArtifactCoordinatesService<T extends AbstractArtifactCoordinates>
        extends CommonCrudService<T>
{

    @Override
    protected boolean identifyEntity(T entity)
    {
        if (super.identifyEntity(entity))
        {
            return true;
        }

        String sQuery = String.format("SELECT FROM INDEX:idx_artifact_coordinates WHERE key = :path");

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        HashMap<String, Object> params = new HashMap<>();
        params.put("path", entity.getPath());

        ORID objectId = ((List<ODocument>) getDelegate().command(oQuery)
                                                        .execute(params)).stream()
                                                                         .limit(1)
                                                                         .map(d -> ((ODocument) d.field("rid")).getIdentity())
                                                                         .findFirst()
                                                                         .orElse(null);
        if (objectId == null)
        {
            return false;
        }

        entity.setObjectId(objectId.toString());

        return true;
    }

}
