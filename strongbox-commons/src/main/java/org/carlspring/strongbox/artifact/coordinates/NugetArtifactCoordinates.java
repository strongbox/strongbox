package org.carlspring.strongbox.artifact.coordinates;

/**
 * @author Sergey Bespalov
 *
 */
public abstract class NugetArtifactCoordinates extends AbstractArtifactCoordinates
{

    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String TYPE = "type";

    public NugetArtifactCoordinates()
    {
        defineCoordinates(ID, VERSION, TYPE);
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
        return getCoordinate(TYPE);
    }

    public void setType(String type)
    {
        setCoordinate(TYPE, type);
    }

}
