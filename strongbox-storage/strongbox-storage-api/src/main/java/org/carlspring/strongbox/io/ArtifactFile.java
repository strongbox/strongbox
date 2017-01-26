package org.carlspring.strongbox.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import java.io.File;

/**
 * This implementation contains additional information about Artifact File itself, such as {@link Repository} and
 * {@link ArtifactCoordinates}. <br>
 * Note that this is only about "File System" (Unix or Windows) as common {@link File}.
 * 
 * @author mtodorov
 * 
 */
public class ArtifactFile
        extends File
{

    private ArtifactCoordinates artifactCoordinates;
    private String repositoryBaseDir;

    public ArtifactFile(String repositoryBaseDir,
                        ArtifactCoordinates artifactCoordinates)
    {
        super(repositoryBaseDir, artifactCoordinates.toPath());
        this.artifactCoordinates = artifactCoordinates;
        this.repositoryBaseDir = repositoryBaseDir;
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }

    public String getRepositoryBaseDir()
    {
        return repositoryBaseDir;
    }

    public void setRepositoryBaseDir(String baseDir)
    {
        this.repositoryBaseDir = baseDir;
    }

}
