package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;

/**
 * @author carlspring
 */
public class MavenArtifactPathConverter
        implements ArtifactPathCoordinateConverter<MavenArtifactCoordinates>
{

    @Override
    public MavenArtifactCoordinates convertPathToCoordinates(String path)
    {
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(path);

        return new MavenArtifactCoordinates(artifact);
    }

    @Override
    public String convertCoordinatesToPath(MavenArtifactCoordinates coordinates)
    {
        return MavenArtifactUtils.convertArtifactToPath(coordinates.toArtifact());
    }

}
