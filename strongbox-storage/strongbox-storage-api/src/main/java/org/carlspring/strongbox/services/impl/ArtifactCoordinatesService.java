package org.carlspring.strongbox.services.impl;

import java.util.HashMap;
import java.util.List;

import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.data.service.CommonCrudService;

import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Component
public class ArtifactCoordinatesService extends CommonCrudService<AbstractArtifactCoordinates>
{

    @Override
    protected boolean identifyEntity(AbstractArtifactCoordinates entity)
    {
        if (super.identifyEntity(entity))
        {
            return true;
        }

        String sQuery = String.format("SELECT FROM INDEX:idx_artifact_coordinates WHERE key = :path");

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        HashMap<String, Object> params = new HashMap<>();
        params.put("path", entity.toPath());

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

    @Override
    protected <S extends AbstractArtifactCoordinates> S cascadeEntitySave(AbstractArtifactCoordinates entity)
    {
        try
        {
            return super.cascadeEntitySave(entity);
        }
        catch (OConcurrentModificationException ex)
        {
            return (S) findOne(entity.getObjectId())
                               .orElseThrow(() -> ex);
        }
    }

    @Override
    public Class<AbstractArtifactCoordinates> getEntityClass()
    {
        return AbstractArtifactCoordinates.class;
    }

}
