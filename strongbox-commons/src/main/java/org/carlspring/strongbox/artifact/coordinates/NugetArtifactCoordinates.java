package org.carlspring.strongbox.artifact.coordinates;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.carlspring.maven.commons.util.ArtifactUtils;

/**
 * @author Sergey Bespalov
 *
 */
public abstract class NugetArtifactCoordinates extends AbstractArtifactCoordinates
{

    private static final String ID = "id";
    private static final String VERSION = "version";

    public NugetArtifactCoordinates()
    {
        defineCoordinates(ID, VERSION);
    }

    @Override
    public String getId()
    {
        return getCoordinate(ID);
    }

    @Override
    public void setId(
                      String id)
    {
        setCoordinate(ID, id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(
                           String version)
    {
        setCoordinate(VERSION, version);
    }

}
