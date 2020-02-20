package org.carlspring.strongbox.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        extends DomainEntity implements Artifact
{

    private String storageId;

    private String repositoryId;

    @Relationship(type = Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES, direction = Relationship.OUTGOING)
    private GenericArtifactCoordinatesEntity artifactCoordinates;

    @Relationship(type = Edges.ARTIFACT_HAS_TAGS, direction = Relationship.OUTGOING)
    private Set<ArtifactTag> tagSet;

    private Map<String, String> checksums;

    private Set<String> filenames = new LinkedHashSet<>();

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;

    private Date created;

    private Integer downloadCount = Integer.valueOf(0);

    private final ArtifactArchiveListing artifactArchiveListing = new ArtifactArchiveListing();

    @Override
    public String getStorageId()
    {
        return storageId;
    }

    @Override
    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    @Override
    public String getRepositoryId()
    {
        return repositoryId;
    }

    @Override
    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Override
    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates.getLayoutArtifactCoordinates();
    }

    @Override
    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = ((LayoutArtifactCoordinatesEntity)artifactCoordinates).getGenericArtifactCoordinates();
    }

    @Override
    public Set<ArtifactTag> getTagSet()
    {
        return tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
    }

    protected void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    @Override
    public Map<String, String> getChecksums()
    {
        return checksums = Optional.ofNullable(checksums).orElse(new HashMap<>());
    }

    protected void setChecksums(Map<String, String> checksums)
    {
        this.checksums = checksums;
    }

    @Override
    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    @Override
    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public Date getLastUpdated()
    {
        return lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    @Override
    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    @Override
    public Date getLastUsed()
    {
        return lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    @Override
    public void setLastUsed(Date lastUsed)
    {
        this.lastUsed = lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(Date created)
    {
        this.created = created;
    }

    @Override
    public Integer getDownloadCount()
    {
        return downloadCount;
    }

    @Override
    public void setDownloadCount(Integer downloadCount)
    {
        this.downloadCount = downloadCount;
    }

    @Override
    public ArtifactArchiveListing getArtifactArchiveListing()
    {
        return artifactArchiveListing ;
    }

    @Override
    @Transient
    public String getArtifactPath()
    {
        return Optional.of(getArtifactCoordinates())
                       .map(c -> c.toPath())
                       .orElseThrow(() -> new IllegalStateException("ArtifactCoordinates required to be set."));
    }
    
    public class ArtifactArchiveListing
            implements Serializable
    {

        public Set<String> getFilenames()
        {
            return ArtifactEntity.this.filenames;
        }

        public void setFilenames(final Set<String> filenames)
        {
            ArtifactEntity.this.filenames = filenames;
        }

    }


}
