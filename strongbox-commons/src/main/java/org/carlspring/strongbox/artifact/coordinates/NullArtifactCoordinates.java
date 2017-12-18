package org.carlspring.strongbox.artifact.coordinates;

/**
 * @author carlspring
 */
public class NullArtifactCoordinates
        extends AbstractArtifactCoordinates
{

    private static final String PATH = "path";


    public NullArtifactCoordinates()
    {
        defineCoordinates(PATH);
    }

    public NullArtifactCoordinates(String path)
    {
        this();
        setPath(path);
    }

    /**
     * WARNING: Unsurprisingly, this is null.
     * @return  null
     */
    @Override
    public String getId()
    {
        return null;
    }    /**
     * WARNING: Unsurprisingly, this is null.
     * @return  null
     */


    @Override
    public void setId(String id)
    {
    }

    /**
     * WARNING: Unsurprisingly, this is null.
     * @return  null
     */
    @Override
    public String getVersion()
    {
        return null;
    }

    @Override
    public void setVersion(String version)
    {
    }

    @Override
    public String toPath()
    {
        return getPath();
    }

    @Override
    public String toString()
    {
        return "NullArtifactCoordinates{path='" + getPath() + '\'' + '}';
    }

}
