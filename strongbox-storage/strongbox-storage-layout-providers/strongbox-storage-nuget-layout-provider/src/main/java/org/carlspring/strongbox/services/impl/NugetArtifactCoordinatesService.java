package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.services.AbstractArtifactCoordinatesService;
import org.springframework.stereotype.Component;

@Component
public class NugetArtifactCoordinatesService
        extends AbstractArtifactCoordinatesService<NugetHierarchicalArtifactCoordinates>
{

    @Override
    public Class<NugetHierarchicalArtifactCoordinates> getEntityClass()
    {
        return NugetHierarchicalArtifactCoordinates.class;
    }

}
