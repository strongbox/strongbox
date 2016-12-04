package org.carlspring.strongbox.artifact.coordinates;

/**
 * @author Sergey Bespalov
 *
 */
public abstract class NugetArtifactCoordinates extends AbstractArtifactCoordinates
{

    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String EXTENSION = "extension";

    public NugetArtifactCoordinates()
    {
        defineCoordinates(ID, VERSION, EXTENSION);
    }

    @Override
    public String getId()
    {
        return getCoordinate(ID);
    }

    @Override
    public void setId(String id)
    {
        setCoordinate(ID, id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    public String getType()
    {
        return getCoordinate(EXTENSION);
    }

    public void setType(String type)
    {
        setCoordinate(EXTENSION, type);
    }

}
