package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;

import java.util.Map;
import java.util.Optional;

/**
 * @author carlspring
 */
@Entity
public class NullArtifactCoordinates
        extends AbstractArtifactCoordinates<NullArtifactCoordinates, NullArtifactCoordinates>
{

    private static final String PATH = "path";


    public NullArtifactCoordinates()
    {
        resetCoordinates(PATH);
    }

    public NullArtifactCoordinates(String path)
    {
        this();
        setCoordinate(PATH, path);
    }

    @Override
    public String getId()
    {
        return getPath();
    }

    @Override
    public void setId(String id)
    {
        setCoordinate(PATH, id);
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
        return Optional.ofNullable(getCoordinate(PATH)).orElse("");
    }

    @Override
    public String toString()
    {
        return "NullArtifactCoordinates{path='" + getPath() + '\'' + '}';
    }

}
