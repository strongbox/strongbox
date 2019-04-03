package org.carlspring.strongbox.artifact.coordinates;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

/*
 * Represents {@link ArtifactCoordinates} for PyPi repository
 * <p>
 * Proper path for this coordinates is in the format of: {id}/{version}/{classifier}
 * Example: strongbox.p2/1.0.0/osgi.bundle
 */

@Entity
@ArtifactCoordinatesLayout("pypi")
public class PyPiArtifactCoordinates
        extends AbstractArtifactCoordinates<PyPiArtifactCoordinates, PyPiArtifactCoordinates>
{
	
	public static final String ID = "id";

    public static final String VERSION = "version";

    public static final String CLASSIFIER = "classifier";

    public static final String FILENAME = "filename";

    private static final String SEPARATOR = "/";
	
	public PyPiArtifactCoordinates(String id,
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
    
    @Override
    public String toPath()
    {
        return getId();
    }
    
    @Override
    public int hashCode()
    {
    	return 0;
    	
    	/*
        int result = getId().hashCode();
        result = 31 * result + getVersion().hashCode();
        result = 31 * result + getClassifier().hashCode();

        return result;
        */
    }

    @Override
    public PyPiArtifactCoordinates getNativeVersion()
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