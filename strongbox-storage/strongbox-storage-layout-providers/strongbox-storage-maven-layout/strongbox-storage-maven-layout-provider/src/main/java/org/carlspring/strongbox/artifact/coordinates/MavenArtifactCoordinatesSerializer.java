package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.data.domain.EntitySerializer;

import org.springframework.stereotype.Component;

@Component
public class MavenArtifactCoordinatesSerializer extends EntitySerializer<MavenArtifactCoordinates>
{

    @Override
    public int getTypeId()
    {
        return 30;
    }

    @Override
    public Class<MavenArtifactCoordinates> getEntityClass()
    {
        return MavenArtifactCoordinates.class;
    }

}
