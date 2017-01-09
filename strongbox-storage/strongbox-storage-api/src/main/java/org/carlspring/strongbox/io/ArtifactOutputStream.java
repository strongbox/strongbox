package org.carlspring.strongbox.io;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

/**
 * This OutputStream wraps a source stream from different Storage types (File System, AWS, JDBC, etc.).
 * 
 * @author Sergey Bespalov
 *
 */
public class ArtifactOutputStream
        extends MultipleDigestOutputStream
{

    private ArtifactCoordinates coordinates;

    public ArtifactOutputStream(OutputStream source, ArtifactCoordinates coordinates) throws NoSuchAlgorithmException
    {
        super(source);
        this.coordinates = coordinates;
    }

    public ArtifactCoordinates getCoordinates()
    {
        return coordinates;
    }

}
