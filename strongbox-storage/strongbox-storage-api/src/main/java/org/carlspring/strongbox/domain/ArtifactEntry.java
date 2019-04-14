package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * @author carlspring
 */
@Entity
public class ArtifactEntry
        extends GenericEntity
{

    private String storageId;

    private String repositoryId;

    // if you have to rename this field please update ArtifactEntryServiceImpl.findByCoordinates() implementation
    @ManyToOne(cascade = { CascadeType.DETACH,
                           CascadeType.MERGE,
                           CascadeType.PERSIST,
                           CascadeType.REFRESH })
    private AbstractArtifactCoordinates artifactCoordinates;

    @ManyToMany(targetEntity = ArtifactTagEntry.class)
    private Set<ArtifactTag> tagSet;

    private Map<String, String> checksums;

    @Embedded
    private ArtifactArchiveListing artifactArchiveListing;

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;

    private Date created;

    private Integer downloadCount = Integer.valueOf(0);

    public ArtifactEntry()
    {
    }

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
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = (AbstractArtifactCoordinates) artifactCoordinates;
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

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\nArtifactEntry{");
        sb.append("storageId='").append(storageId).append('\'');
        sb.append(", repositoryId='").append(repositoryId).append('\'');
        sb.append(", artifactCoordinates=").append(artifactCoordinates).append('\n');
        sb.append(", tagSet=").append(tagSet);
        sb.append(", checksums=").append(checksums);
        sb.append(", objectId='").append(objectId).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", artifactArchiveListing=").append(artifactArchiveListing);
        sb.append(", entityVersion=").append(entityVersion);
        sb.append(", sizeInBytes=").append(sizeInBytes);
        sb.append(", lastUpdated=").append(lastUpdated);
        sb.append(", lastUsed=").append(lastUsed);
        sb.append(", created=").append(created);
        sb.append(", downloadCount=").append(downloadCount);
        sb.append('}').append('\n');

        return sb.toString();
    }
}
