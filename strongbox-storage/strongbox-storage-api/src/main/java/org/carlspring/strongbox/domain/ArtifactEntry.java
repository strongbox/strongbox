package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

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

//    private ArtifactCoordinates artifactCoordinates;

    /*
    private ArtifactMetadata artifactMetadata;

    private List<ArtifactDependency> artifactDependencies = new ArrayList<>();

    private List<String> authors = new ArrayList<>();

    private String projectUrl;

    private String vcsUrl;

    private List<String> artifactFilePaths;
    */


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

  /*  public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }
    */

    @Override
    public String toString()
    {
        return "ArtifactEntry{" +
               "storageId='" + storageId + '\'' +
               ", repositoryId='" + repositoryId + '\'' +
               '}';
    }
}
