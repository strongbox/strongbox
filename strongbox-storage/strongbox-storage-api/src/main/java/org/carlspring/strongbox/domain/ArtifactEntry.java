package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

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
    private ArtifactCoordinates artifactCoordinates;

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
        this.artifactCoordinates = artifactCoordinates;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                          .add("objectId", objectId)
                          .add("storageId", storageId)
                          .add("repositoryId", repositoryId)
                          .add("artifactCoordinates", artifactCoordinates)
                          .toString();
    }
}
