package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.domain.GenericEntityHook;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;
import java.io.Serializable;

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
    @OneToOne(orphanRemoval = true)
    private AbstractArtifactCoordinates artifactCoordinates;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private ArtifactAttributes artifactAttributes;

    /**
     * This field is used as part of [storageId, repositoryId, artifactPath] unique index. The value of this field is
     * populated within {@link GenericEntityHook}.
     */
    private String artifactPath;

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

    public ArtifactAttributes getArtifactAttributes()
    {
        return artifactAttributes != null ? artifactAttributes : (artifactAttributes = new ArtifactAttributes());
    }

    public void setArtifactAttributes(ArtifactAttributes artifactAttributes)
    {
        this.artifactAttributes = artifactAttributes;
    }

    public final String getArtifactPath()
    {
        return artifactCoordinates == null ? artifactPath : (artifactPath = artifactCoordinates.toPath());
    }

    public void setArtifactPath(String artifactPath)
    {
        this.artifactPath = artifactCoordinates != null ? artifactCoordinates.toPath() : artifactPath;
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
        sb.append(", \n\tartifactAttributes='")
          .append(artifactAttributes)
          .append('\'');
        sb.append(", \n\tartifactCoordinates=")
          .append(artifactCoordinates);
        sb.append('}');
        return sb.toString();
    }
}
