package org.carlspring.strongbox.artifact.coordinates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author carlspring
 */
public class ArtifactCoordinates
{

    private Map<String, String> coordinates = new LinkedHashMap<>();


    public ArtifactCoordinates()
    {
    }

    public ArtifactCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
    }

    public void defineCoordinates(String... coordinates)
    {
        for (String coordinate : coordinates)
        {
            this.coordinates.put(coordinate, null);
        }
    }

    public void defineCoordinate(String coordinate)
    {
        coordinates.put(coordinate, null);
    }

    public String getCoordinate(String coordinate)
    {
        return coordinates.get(coordinate);
    }

    public String setCoordinate(String coordinate, String value)
    {
        return coordinates.put(coordinate, value);
    }

    public Map<String, String> getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
    }

}
