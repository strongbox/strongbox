package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public class ArtifactFile extends File
{

    private Repository repository;

    private Artifact artifact;

    private long temporaryTimestamp;

    private boolean temporaryMode = false;


    public ArtifactFile(Repository repository, Artifact artifact)
    {
        this(repository, artifact, false);
    }

    public ArtifactFile(Repository repository, Artifact artifact, boolean temporaryMode)
    {
        super(repository.getBasedir(), ArtifactUtils.convertArtifactToPath(artifact));

        this.repository = repository;
        this.artifact = artifact;
        this.temporaryMode = temporaryMode;
        this.temporaryTimestamp = System.currentTimeMillis();
    }

    public ArtifactFile(File file)
    {
        super(file.getAbsolutePath());
    }

    public ArtifactFile getTemporaryFile()
    {
        return new ArtifactFile(new File(repository.getBasedir() +
                                         "/.temp/" +
                                         ArtifactUtils.convertArtifactToPath(artifact) +
                                         "." + temporaryTimestamp));
    }

    public void createParents()
    {
        if (isTemporaryMode())
        {
            if (!getTemporaryFile().getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                getTemporaryFile().getParentFile().mkdirs();
            }
        }
        else
        {
            if (!getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                getParentFile().mkdirs();
            }
        }
    }

    public void moveTempFileToOriginalDestination()
            throws IOException
    {
        if (!getParentFile().exists())
        {
            //noinspection ResultOfMethodCallIgnored
            getParentFile().mkdirs();
        }

        if (this.exists())
        {
            FileUtils.forceDelete(this);
        }

        FileUtils.moveFile(getTemporaryFile(), this);
    }

    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    public long getTemporaryTimestamp()
    {
        return temporaryTimestamp;
    }

    public void setTemporaryTimestamp(long temporaryTimestamp)
    {
        this.temporaryTimestamp = temporaryTimestamp;
    }

    public boolean isTemporaryMode()
    {
        return temporaryMode;
    }

    public void setTemporaryMode(boolean temporaryMode)
    {
        this.temporaryMode = temporaryMode;
    }

}
