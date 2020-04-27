package org.carlspring.strongbox.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Sergey Bespalov
 */
@NodeEntity(Vertices.REMOTE_ARTIFACT)
public class RemoteArtifactEntity extends DomainEntity implements RemoteArtifact
{

    @Relationship(type = Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT, direction = Relationship.OUTGOING)
    private ArtifactEntity artifact = new ArtifactEntity();

    private Boolean cached = Boolean.FALSE;

    public RemoteArtifactEntity(String storageId,
                                String repositoryId,
                                ArtifactCoordinates artifactCoordinates)
    {
        this(new ArtifactEntity(storageId, repositoryId, artifactCoordinates));
    }

    RemoteArtifactEntity()
    {
    }

    public RemoteArtifactEntity(ArtifactEntity artifactEntity)
    {
        this.artifact = artifactEntity;
        artifactEntity.setHierarchyChild(this);
        if (getArtifactCoordinates() != null)
        {
            setUuid(String.format("%s-%s-%s", getStorageId(), getRepositoryId(), getArtifactCoordinates().buildPath()));
        }
    }

    @Override
    public Artifact getHierarchyChild()
    {
        return null;
    }

    @Override
    public Artifact getHierarchyParent()
    {
        return artifact;
    }

    public void setUuid(String uuid)
    {
        super.setUuid(uuid);
        artifact.setUuid(uuid);
    }

    public Boolean getIsCached()
    {
        return cached;
    }

    public void setIsCached(Boolean isCached)
    {
        this.cached = isCached;
    }

    public String getStorageId()
    {
        return artifact.getStorageId();
    }

    public void setStorageId(String storageId)
    {
        artifact.setStorageId(storageId);
    }

    public String getRepositoryId()
    {
        return artifact.getRepositoryId();
    }

    public void setRepositoryId(String repositoryId)
    {
        artifact.setRepositoryId(repositoryId);
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifact.getArtifactCoordinates();
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        artifact.setArtifactCoordinates(artifactCoordinates);
    }

    public Set<ArtifactTag> getTagSet()
    {
        return artifact.getTagSet();
    }

    public Map<String, String> getChecksums()
    {
        return artifact.getChecksums();
    }

    public void setChecksums(Map<String, String> checksums)
    {
        artifact.setChecksums(checksums);
    }

    public Long getSizeInBytes()
    {
        return artifact.getSizeInBytes();
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        artifact.setSizeInBytes(sizeInBytes);
    }

    public LocalDateTime getLastUpdated()
    {
        return artifact.getLastUpdated();
    }

    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        artifact.setLastUpdated(lastUpdated);
    }

    public LocalDateTime getLastUsed()
    {
        return artifact.getLastUsed();
    }

    public void setLastUsed(LocalDateTime lastUsed)
    {
        artifact.setLastUsed(lastUsed);
    }

    public LocalDateTime getCreated()
    {
        return artifact.getCreated();
    }

    public void setCreated(LocalDateTime created)
    {
        artifact.setCreated(created);
    }

    public Integer getDownloadCount()
    {
        return artifact.getDownloadCount();
    }

    public void setDownloadCount(Integer downloadCount)
    {
        artifact.setDownloadCount(downloadCount);
    }

    public String getArtifactPath()
    {
        return artifact.getArtifactPath();
    }

    @Override
    public Set<ArtifactArchiveListing> getArtifactArchiveListings()
    {
        return artifact.getArtifactArchiveListings();
    }

    @Override
    public void setArtifactArchiveListings(Set<ArtifactArchiveListing> artifactArchiveListings)
    {
        artifact.setArtifactArchiveListings(artifactArchiveListings);
    }

}
