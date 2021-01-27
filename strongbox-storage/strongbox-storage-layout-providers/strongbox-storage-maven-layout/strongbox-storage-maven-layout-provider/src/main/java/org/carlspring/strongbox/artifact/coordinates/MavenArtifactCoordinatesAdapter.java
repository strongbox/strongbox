package org.carlspring.strongbox.artifact.coordinates;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.adapters.LayoutArtifactCoordinatesAdapter;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class MavenArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesAdapter<MavenArtifactCoordinates, ComparableVersion>
{

    public MavenArtifactCoordinatesAdapter()
    {
        super(Vertices.MAVEN_ARTIFACT_COORDINATES, MavenArtifactCoordinates.class);
    }

    @Override
    protected MavenArtifactCoordinates newInstance()
    {
        return new MavenArtifactCoordinates();
    }
    
}
