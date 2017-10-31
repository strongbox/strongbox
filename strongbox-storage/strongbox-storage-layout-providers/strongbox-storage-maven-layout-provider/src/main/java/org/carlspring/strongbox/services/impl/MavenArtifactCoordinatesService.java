package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.services.AbstractArtifactCoordinatesService;
import org.springframework.stereotype.Component;

@Component
public class MavenArtifactCoordinatesService
        extends AbstractArtifactCoordinatesService<MavenArtifactCoordinates>
{

    @Override
    public Class<MavenArtifactCoordinates> getEntityClass()
    {
        return MavenArtifactCoordinates.class;
    }

}
