package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private AbstractArtifactCoordinates artifactCoordinates;

    @ManyToMany(targetEntity = ArtifactTagEntry.class)
    private Set<ArtifactTag> tagSet;
    
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
        final StringBuilder sb = new StringBuilder("ArtifactEntry{");
        sb.append("\n\tstorageId='")
          .append(storageId)
          .append('\'');
        sb.append(", \n\trepositoryId='")
          .append(repositoryId)
          .append('\'');
        sb.append(", \n\tsizeInBytes='")
          .append(sizeInBytes)
          .append('\'');
        sb.append(", \n\tcreated='")
          .append(created)
          .append('\'');
        sb.append(", \n\tlastUpdated='")
          .append(lastUpdated)
          .append('\'');
        sb.append(", \n\tlastUsed='")
          .append(lastUsed)
          .append('\'');
        sb.append(", \n\tartifactCoordinates=")
          .append(artifactCoordinates);
        sb.append('}');
        return sb.toString();
    }

}
