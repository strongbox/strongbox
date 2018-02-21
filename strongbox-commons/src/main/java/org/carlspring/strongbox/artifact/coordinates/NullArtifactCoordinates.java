package org.carlspring.strongbox.artifact.coordinates;

import java.util.Map;

/**
 * @author carlspring
 */
public class NullArtifactCoordinates
        extends AbstractArtifactCoordinates<NullArtifactCoordinates, NullArtifactCoordinates>
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
    }

    /**
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
    public NullArtifactCoordinates getNativeVersion()
    {
        return this;
    }

    @Override
    public Map<String, String> dropVersion()
    {
        return getCoordinates();
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
