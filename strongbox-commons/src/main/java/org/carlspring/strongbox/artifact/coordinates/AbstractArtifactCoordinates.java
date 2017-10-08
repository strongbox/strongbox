package org.carlspring.strongbox.artifact.coordinates;

import java.util.LinkedHashMap;
import java.util.Map;

import org.carlspring.strongbox.data.domain.GenericEntity;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author carlspring
 */
public abstract class AbstractArtifactCoordinates
        extends GenericEntity
        implements ArtifactCoordinates
{

    private Map<String, String> coordinates = new LinkedHashMap<>();
    private String path;

    public AbstractArtifactCoordinates()
    {
    }

    public AbstractArtifactCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.path = toPath();
    }
    
    public final void defineCoordinates(String... coordinates)
    {
        for (String coordinate : coordinates)
        {
            this.coordinates.put(coordinate, null);
        }
        this.path = toPath();
    }

    @Override
    public void dump()
    {
        for (String coordinateName : coordinates.keySet())
        {
            String coordinateValue = coordinates.get(coordinateName);

            System.out.println(coordinateName + " : " + coordinateValue);
        }
    }

    public final void defineCoordinate(String coordinate)
    {
        coordinates.put(coordinate, null);
        this.path = toPath();
    }

    public String getCoordinate(String coordinate)
    {
        return coordinates.get(coordinate);
    }

    public final String setCoordinate(String coordinate,
                                String value)
    {
        String result = coordinates.put(coordinate, value);
        this.path = toPath();
        return result;
    }

    public Map<String, String> getCoordinates()
    {
        return Collections.unmodifiableMap(coordinates);
    }

    public final void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.path = toPath();
    }
    
    public String getPath()
    {
        return path;
    }

    @Override
    public String toString()
    {
        return toPath();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof AbstractArtifactCoordinates))
        {
            return false;
        }
        AbstractArtifactCoordinates c = (AbstractArtifactCoordinates) obj;
        return c.getCoordinates() == null ? getCoordinates() != null : c.getCoordinates().equals(getCoordinates());
    }

    @Override
    public int hashCode()
    {
        return getCoordinates() == null ? 0 : getCoordinates().hashCode();
    }

}
