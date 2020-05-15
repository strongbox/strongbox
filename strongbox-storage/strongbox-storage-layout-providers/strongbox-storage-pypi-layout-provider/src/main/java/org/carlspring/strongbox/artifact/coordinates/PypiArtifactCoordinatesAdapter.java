package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.adapters.LayoutArtifactCoordinatesAdapter;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class PypiArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesAdapter<PypiArtifactCoordinates, SemanticVersion>
{

    public PypiArtifactCoordinatesAdapter()
    {
        super(Vertices.PYPI_ARTIFACT_COORDINATES, PypiArtifactCoordinates.class);
    }

    @Override
    protected PypiArtifactCoordinates newInstance()
    {
        return new PypiArtifactCoordinates();
    }
    
}
