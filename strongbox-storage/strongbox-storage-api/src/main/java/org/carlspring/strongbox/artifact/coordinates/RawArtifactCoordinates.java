package org.carlspring.strongbox.artifact.coordinates;

import java.util.Map;
import java.util.Optional;

import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author carlspring
 */
@NodeEntity(Vertices.RAW_ARTIFACT_COORDINATES)
public class RawArtifactCoordinates
        extends LayoutArtifactCoordinatesEntity<RawArtifactCoordinates, RawArtifactCoordinates>
{

    public static final String LAYOUT_NAME = "Null Layout";
    private static final String PATH = "path";

    public RawArtifactCoordinates()
    {
        resetCoordinates(PATH);
    }

    public RawArtifactCoordinates(String path)
    {
        setCoordinate(PATH, path);
    }

    @Override
    public String getId()
    {
        return getCoordinate(PATH);
    }

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
    public RawArtifactCoordinates getNativeVersion()
    {
        return this;
    }

    @Override
    public Map<String, String> dropVersion()
    {
        return getCoordinates();
    }
    
    @Override
    public String convertToPath(RawArtifactCoordinates artifactCoordinates)
    {
        return Optional.ofNullable(artifactCoordinates.getCoordinate(PATH)).orElse("");
    }

}
