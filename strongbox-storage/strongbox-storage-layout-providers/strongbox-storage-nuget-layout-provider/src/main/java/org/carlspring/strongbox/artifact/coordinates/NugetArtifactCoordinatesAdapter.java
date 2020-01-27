package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.adapters.LayoutArtifactCoordinatesAdapter;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class NugetArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesAdapter<NugetArtifactCoordinates, SemanticVersion>
{

    public NugetArtifactCoordinatesAdapter()
    {
        super(Vertices.NUGET_ARTIFACT_COORDINATES, NugetArtifactCoordinates.class);
    }

    @Override
    protected NugetArtifactCoordinates newInstance()
    {
        return new NugetArtifactCoordinates();
    }
    
}
