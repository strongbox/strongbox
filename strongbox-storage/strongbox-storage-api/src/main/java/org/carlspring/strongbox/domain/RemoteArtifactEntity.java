package org.carlspring.strongbox.domain;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.ArtifactEntity.ArtifactArchiveListing;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Sergey Bespalov
 */
@NodeEntity(Vertices.REMOTE_ARTIFACT)
public class RemoteArtifactEntity extends DomainEntity implements RemoteArtifact
{

    @Relationship(type = Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT, direction = Relationship.OUTGOING)
    private final ArtifactEntity artifact;
    private Boolean isCached = Boolean.FALSE;

    public RemoteArtifactEntity()
    {
        this(new ArtifactEntity());
    }

    public RemoteArtifactEntity(ArtifactEntity artifactEntity)
    {
        this.artifact = artifactEntity;
    }

    public void setUuid(String uuid)
    {
        super.setUuid(uuid);
        artifact.setUuid(uuid);
    }

    public Boolean getIsCached()
    {
        return isCached;
    }

    public void setIsCached(Boolean isCached)
    {
        this.isCached = isCached;
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

    public Long getSizeInBytes()
    {
        return artifact.getSizeInBytes();
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        artifact.setSizeInBytes(sizeInBytes);
    }

    public Date getLastUpdated()
    {
        return artifact.getLastUpdated();
    }

    public void setLastUpdated(Date lastUpdated)
    {
        artifact.setLastUpdated(lastUpdated);
    }

    public Date getLastUsed()
    {
        return artifact.getLastUsed();
    }

    public void setLastUsed(Date lastUsed)
    {
        artifact.setLastUsed(lastUsed);
    }

    public Date getCreated()
    {
        return artifact.getCreated();
    }

    public void setCreated(Date created)
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

    public ArtifactArchiveListing getArtifactArchiveListing()
    {
        return artifact.getArtifactArchiveListing();
    }

    public String getArtifactPath()
    {
        return artifact.getArtifactPath();
    }

    public boolean booleanValue()
    {
        return isCached.booleanValue();
    }

    public String toString()
    {
        return isCached.toString();
    }

    public int hashCode()
    {
        return isCached.hashCode();
    }

    public boolean equals(Object obj)
    {
        return isCached.equals(obj);
    }

    public int compareTo(Boolean b)
    {
        return isCached.compareTo(b);
    }

}
