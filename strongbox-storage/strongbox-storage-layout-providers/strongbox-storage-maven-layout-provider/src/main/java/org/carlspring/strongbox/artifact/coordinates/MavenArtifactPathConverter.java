package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.maven.commons.util.ArtifactUtils;

import org.apache.maven.artifact.Artifact;

/**
 * @author carlspring
 */
public class MavenArtifactPathConverter
        implements ArtifactPathCoordinateConverter<MavenArtifactCoordinates>
{

    @Override
    public MavenArtifactCoordinates convertPathToCoordinates(String path)
    {
        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);

        return new MavenArtifactCoordinates(artifact);
    }

    @Override
    public String convertCoordinatesToPath(MavenArtifactCoordinates coordinates)
    {
        return ArtifactUtils.convertArtifactToPath(coordinates.toArtifact());
    }

}
