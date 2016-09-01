package org.carlspring.strongbox.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * @author mtodorov
 */
public class ArtifactFile
        extends File
{

    private Repository repository;

    private ArtifactCoordinates artifactCoordinates;

    private long temporaryTimestamp;

    private boolean temporaryMode;


    public ArtifactFile(Repository repository,
                        ArtifactCoordinates artifactCoordinates)
    {
        this(repository, artifactCoordinates, false);
    }

    public ArtifactFile(Repository repository,
                        ArtifactCoordinates artifactCoordinates,
                        boolean temporaryMode)
    {
        super(repository.getBasedir(), artifactCoordinates.toPath());

        this.repository = repository;
        this.artifactCoordinates = artifactCoordinates;
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
                                         artifactCoordinates.toPath() +
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

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
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
