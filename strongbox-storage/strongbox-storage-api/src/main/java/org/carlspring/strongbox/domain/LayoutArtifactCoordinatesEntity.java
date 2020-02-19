package org.carlspring.strongbox.domain;

import java.net.URI;
import java.util.Map;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author sbespalov
 */
public abstract class LayoutArtifactCoordinatesEntity<T extends LayoutArtifactCoordinatesEntity<T, V>, V extends Comparable<V>>
        extends DomainEntity
        implements ArtifactCoordinates<T, V>
{

    @Relationship(type = Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES, direction = Relationship.OUTGOING)
    private final GenericArtifactCoordinatesEntity genericArtifactCoordinates;
    private String path;

    public LayoutArtifactCoordinatesEntity()
    {
        this(new GenericArtifactCoordinatesEntity());
    }

    public LayoutArtifactCoordinatesEntity(GenericArtifactCoordinatesEntity genericArtifactCoordinates)
    {
        this.genericArtifactCoordinates = genericArtifactCoordinates;
    }

    public GenericArtifactCoordinatesEntity getGenericArtifactCoordinates()
    {
        return genericArtifactCoordinates;
    }

    public void setUuid(String uuid)
    {
        super.setUuid(uuid);
        genericArtifactCoordinates.setUuid(uuid);
    }

    public String getVersion()
    {
        return genericArtifactCoordinates.getVersion();
    }

    public void setVersion(String version)
    {
        genericArtifactCoordinates.setVersion(version);
    }

    public Map<String, String> getCoordinates()
    {
        return genericArtifactCoordinates.getCoordinates();
    }

    protected final void resetCoordinates(String... coordinates)
    {
        genericArtifactCoordinates.resetCoordinates(coordinates);
        this.path = null;
    }

    protected final void defineCoordinate(String coordinate)
    {
        genericArtifactCoordinates.defineCoordinate(coordinate);
        this.path = toPath();
    }

    protected final String setCoordinate(String coordinate,
                                         String value)
    {
        String result = genericArtifactCoordinates.setCoordinate(coordinate, value);
        this.path = toPath();
        return result;
    }

    protected String getCoordinate(String coordinate)
    {
        return genericArtifactCoordinates.getCoordinate(coordinate);
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public URI toResource()
    {
        return URI.create(toPath());
    }

    @Override
    public int compareTo(T that)
    {
        if (that == null)
        {
            return -1;
        }

        int result = ((result = compareId(that)) == 0 ? compareVersion(that) : result);

        return result;
    }

    protected int compareVersion(T that)
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

    protected int compareId(T that)
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

}
