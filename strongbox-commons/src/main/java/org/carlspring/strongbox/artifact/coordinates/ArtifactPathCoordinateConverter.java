package org.carlspring.strongbox.artifact.coordinates;

/**
 * @author carlspring
 */
public interface ArtifactPathCoordinateConverter<T extends ArtifactCoordinates>
{

    ArtifactCoordinates convertPathToCoordinates(String path);

    String convertCoordinatesToPath(T coordinates);

}
