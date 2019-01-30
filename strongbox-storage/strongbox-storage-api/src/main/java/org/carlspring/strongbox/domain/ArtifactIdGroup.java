package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import javax.persistence.Entity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class ArtifactIdGroup
        extends ArtifactGroup
{

    public ArtifactIdGroup()
    {
        super();
    }

    public ArtifactIdGroup(final String name)
    {
        super(name);
    }

    public String getArtifactId()
    {
        return getName();
    }

    public static String createName(ArtifactCoordinates artifactCoordinates)
    {
        return String.format("%s/%s", artifactCoordinates.getClass().getSimpleName(), artifactCoordinates.getId());
    }

}
