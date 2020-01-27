package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.adapters.LayoutArtifactCoordinatesAdapter;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class NpmArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesAdapter<NpmArtifactCoordinates, SemanticVersion>
{

    public NpmArtifactCoordinatesAdapter()
    {
        super(Vertices.NPM_ARTIFACT_COORDINATES, NpmArtifactCoordinates.class);
    }

    @Override
    protected NpmArtifactCoordinates newInstance()
    {
        return new NpmArtifactCoordinates();
    }
    
}
