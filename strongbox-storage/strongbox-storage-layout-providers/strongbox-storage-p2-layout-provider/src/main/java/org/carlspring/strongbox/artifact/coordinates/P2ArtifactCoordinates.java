package org.carlspring.strongbox.artifact.coordinates;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

/**
 * Represents {@link ArtifactCoordinates} for P2 repository
 * <p>
 * Proper path for this coordinates is in the format of: {id}/{version}/{classifier}
 * Example: strongbox.p2/1.0.0/osgi.bundle
 */
@Entity
@ArtifactCoordinatesLayout("p2")
public class P2ArtifactCoordinates
        extends AbstractArtifactCoordinates<P2ArtifactCoordinates, P2ArtifactCoordinates>
{

    public static final String ID = "id";

    public static final String VERSION = "version";

    public static final String CLASSIFIER = "classifier";

    public static final String FILENAME = "filename";

    private static final String SEPARATOR = "/";

    private Map<String, String> properties = new HashMap<>();

    public P2ArtifactCoordinates(String id,
                                 String version,
                                 String classifier)
    {
        if (id == null || version == null || classifier == null)
        {
            throw new IllegalArgumentException("Id, version and classifier must be specified");
        }
        setId(id);
        setVersion(version);
        setCoordinate(CLASSIFIER, classifier);
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

    public String getClassifier()
    {
        return getCoordinate(CLASSIFIER);
    }

    @Override
    public String toPath()
    {
        return getId() + SEPARATOR + getVersion() + SEPARATOR + getClassifier();
    }

    public void setFilename(String filename)
    {
        setCoordinate(FILENAME, filename);
    }

    public String getFilename()
    {
        return getCoordinate(FILENAME);
    }

    public static P2ArtifactCoordinates create(String path)
    {
        if (path != null && path.length() > 0 && path.contains(SEPARATOR))
        {
            String[] splitedSeparator = path.split("/");
            if (splitedSeparator.length == 3)
            {
                return new P2ArtifactCoordinates(splitedSeparator[0], splitedSeparator[1], splitedSeparator[2]);
            }
        }

        throw new IllegalArgumentException("Path is not properly formatted");
    }

    public void addProperty(String key,
                            String value)
    {
        properties.put(key, value);
    }

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        P2ArtifactCoordinates that = (P2ArtifactCoordinates) o;

        if (!getId().equals(that.getId()))
        {
            return false;
        }
        if (!getVersion().equals(that.getVersion()))
        {
            return false;
        }

        return getClassifier().equals(that.getClassifier());
    }

    @Override
    public int hashCode()
    {
        int result = getId().hashCode();
        result = 31 * result + getVersion().hashCode();
        result = 31 * result + getClassifier().hashCode();

        return result;
    }

    @Override
    public P2ArtifactCoordinates getNativeVersion()
    {
        return null;
    }
    
    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }
    
}
