package org.carlspring.strongbox.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.domain.GenericEntityHook;

/**
 * @author carlspring
 */
public class ArtifactEntry
        extends GenericEntity
        implements Serializable
{

    private String storageId;

    private String repositoryId;

    // if you have to rename this field please update ArtifactEntryServiceImpl.findByCoordinates() implementation
    @ManyToOne
    private AbstractArtifactCoordinates artifactCoordinates;

    @ManyToMany(targetEntity = ArtifactTagEntry.class)
    private Set<ArtifactTag> tagSet;
    
    /**
     * This field is used as part of [storageId, repositoryId, artifactPath] unique index. The value of this field is
     * populated within {@link GenericEntityHook}.
     */
    private String artifactPath;

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;
    
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
        getArtifactPath();
    }

    public Set<ArtifactTag> getTagSet()
    {
        return tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
    }

    protected void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    public final String getArtifactPath()
    {
        return artifactCoordinates == null ? artifactPath : (artifactPath = artifactCoordinates.toPath());
    }

    public void setArtifactPath(String artifactPath)
    {
        this.artifactPath = artifactCoordinates != null ? artifactCoordinates.toPath() : artifactPath;
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

    public Integer getDownloadCount()
    {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount)
    {
        this.downloadCount = downloadCount;
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
