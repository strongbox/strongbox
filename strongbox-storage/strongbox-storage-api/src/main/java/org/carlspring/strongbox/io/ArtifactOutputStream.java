package org.carlspring.strongbox.io;

import java.io.FilterOutputStream;
import java.io.OutputStream;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

/**
 * This OutputStream wraps a source stream from different Storage types (File System, AWS, JDBC, etc.).
 * 
 * @author Sergey Bespalov
 *
 */
public class ArtifactOutputStream
        extends FilterOutputStream
{

    private ArtifactCoordinates coordinates;

    public ArtifactOutputStream(OutputStream source, ArtifactCoordinates coordinates)
    {
        super(source);
        this.coordinates = coordinates;
    }

    public ArtifactCoordinates getCoordinates()
    {
        return coordinates;
    }

}
