package org.carlspring.strongbox.artifact.coordinates;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.carlspring.strongbox.data.domain.GenericEntity;


/**
 * @author carlspring
 */
public abstract class AbstractArtifactCoordinates<C extends AbstractArtifactCoordinates<C, V>, V extends Comparable<V>>
        extends GenericEntity
        implements ArtifactCoordinates<C, V>
{

    private Map<String, String> coordinates = new LinkedHashMap<>();
    /**
     * This field is used as unique OrientDB index.
     */
    private String path;

    public AbstractArtifactCoordinates()
    {
    }

    public AbstractArtifactCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.path = toPath();
    }
    
    protected final void defineCoordinates(String... coordinates)
    {
        for (String coordinate : coordinates)
        {
            this.coordinates.put(coordinate, null);
        }
        this.path = toPath();
    }

    public void dump()
    {
        for (String coordinateName : coordinates.keySet())
        {
            String coordinateValue = coordinates.get(coordinateName);

            System.out.println(coordinateName + " : " + coordinateValue);
        }
    }

    protected final void defineCoordinate(String coordinate)
    {
        coordinates.put(coordinate, null);
        this.path = toPath();
    }

    protected String getCoordinate(String coordinate)
    {
        return coordinates.get(coordinate);
    }

    protected final String setCoordinate(String coordinate,
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

    protected final void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
        this.path = toPath();
    }
    
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @Override
    public URI toResource()
    {
        return URI.create(toPath());
    }
    
    @Override
    public int compareTo(C that)
    {
        if (that == null)
        {
            return -1;
        }

        int result = ((result = compareId(that)) == 0 ? compareVersion(that) : result);

        return result;
    }
    
    protected int compareVersion(C that)
    {
        V thisNativeVersion = getNativeVersion();
        V thatNativeVersion = that.getNativeVersion();
        
        if (thisNativeVersion == null && thatNativeVersion == null)
        {
            String thisVersion = getVersion();
            String thatVersion = that.getVersion();

            return compareToken(thisVersion, thatVersion);
        }
        
        return compareToken(thisNativeVersion, thatNativeVersion);        
    }

    protected int compareId(C that)
    {
        String thisId = getId();
        String thatId = that.getId();

        return compareToken(thisId, thatId);
    }

    protected <T extends Comparable<T>> int compareToken(T thisId,
                                                         T thatId)
    {
        if (thisId == thatId)
        {
            return 0;
        }
        if (thisId == null)
        {
            return Boolean.compare(true, thatId == null);
        }
        return thatId == null ? 1 : Integer.signum(thisId.compareTo(thatId));
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
