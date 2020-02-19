package org.carlspring.strongbox.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author carlspring
 * @author sbespalov
 */
@NodeEntity(Vertices.ARTIFACT)
public class ArtifactEntity
        extends DomainEntity
{

    private String storageId;

    private String repositoryId;

    @Relationship(type = Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES, direction = Relationship.OUTGOING)
    private GenericArtifactCoordinatesEntity artifactCoordinates;

    @Relationship(type = Edges.ARTIFACT_HAS_TAGS, direction = Relationship.OUTGOING)
    private Set<ArtifactTag> tagSet;

    private Map<String, String> checksums;

    @Embedded
    private ArtifactArchiveListing artifactArchiveListing;

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;

    private Date created;

    private Integer downloadCount = Integer.valueOf(0);

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates.getLayoutArtifactCoordinates();
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = ((LayoutArtifactCoordinatesEntity)artifactCoordinates).getGenericArtifactCoordinates();
    }

    public Set<ArtifactTag> getTagSet()
    {
        return tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
    }

    protected void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    public Map<String, String> getChecksums()
    {
        return checksums = Optional.ofNullable(checksums).orElse(new HashMap<>());
    }

    protected void setChecksums(Map<String, String> checksums)
    {
        this.checksums = checksums;
    }

    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public Date getLastUpdated()
    {
        return lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    public Date getLastUsed()
    {
        return lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    public void setLastUsed(Date lastUsed)
    {
        this.lastUsed = lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Integer getDownloadCount()
    {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount)
    {
        this.downloadCount = downloadCount;
    }

    public ArtifactArchiveListing getArtifactArchiveListing()
    {
        return artifactArchiveListing;
    }

    public void setArtifactArchiveListing(final ArtifactArchiveListing artifactArchiveListing)
    {
        this.artifactArchiveListing = artifactArchiveListing;
    }

    @Transient
    public String getArtifactPath()
    {
        return Optional.of(getArtifactCoordinates())
                       .map(c -> c.toPath())
                       .orElseThrow(() -> new IllegalStateException("ArtifactCoordinates required to be set."));
    }

}
