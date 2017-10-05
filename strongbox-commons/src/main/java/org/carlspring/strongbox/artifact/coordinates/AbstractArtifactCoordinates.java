package org.carlspring.strongbox.artifact.coordinates;

import java.util.LinkedHashMap;
import java.util.Map;

import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * @author carlspring
 */
public abstract class AbstractArtifactCoordinates
        extends GenericEntity
        implements ArtifactCoordinates
{

    private Map<String, String> coordinates = new LinkedHashMap<>();


    public AbstractArtifactCoordinates()
    {
    }

    public AbstractArtifactCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.uuid = toPath();
    }
    
    @Override
    public void setUuid(String uuid)
    {
    }

    public void defineCoordinates(String... coordinates)
    {
        for (String coordinate : coordinates)
        {
            this.coordinates.put(coordinate, null);
        }
        this.uuid = toPath();
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

    public void defineCoordinate(String coordinate)
    {
        coordinates.put(coordinate, null);
        this.uuid = toPath();
    }

    public String getCoordinate(String coordinate)
    {
        return coordinates.get(coordinate);
    }

    public String setCoordinate(String coordinate,
                                String value)
    {
        String result = coordinates.put(coordinate, value);
        this.uuid = toPath();
        return result;
    }

    public Map<String, String> getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.uuid = toPath();
    }

    @Override
    public String toString()
    {
        return toPath();
    }

}
