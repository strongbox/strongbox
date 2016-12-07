package org.carlspring.strongbox.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactFile
        extends File
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactFile.class);

    private Repository repository;

    private ArtifactCoordinates artifactCoordinates;

    private long temporaryTimestamp = System.nanoTime();

    private boolean temporaryMode;

    /**
     * This is to only be used internally in case the artifact coordinates have not been passed in.
     */
    private String relativePath;


    public ArtifactFile(Repository repository,
                        ArtifactCoordinates artifactCoordinates)
    {
        this(repository, artifactCoordinates, false);
    }

    public ArtifactFile(Repository repository,
                        String path)
    {
        this(repository, path, false);
    }

    public ArtifactFile(Repository repository,
                        String path,
                        boolean temporaryMode)
    {
        this(new File(repository.getBasedir(), path));

        this.repository = repository;
        this.relativePath = path;
        this.temporaryMode = temporaryMode;
    }

    public ArtifactFile(Repository repository,
                        ArtifactCoordinates artifactCoordinates,
                        boolean temporaryMode)
    {
        super(repository.getBasedir(), artifactCoordinates.toPath());

        this.repository = repository;
        this.artifactCoordinates = artifactCoordinates;
        this.temporaryMode = temporaryMode;
    }

    public ArtifactFile(File file)
    {
        super(file.getAbsolutePath());
        this.temporaryMode = true;
    }

    public ArtifactFile getTemporaryFile()
    {
        File tempFile = new File(repository.getBasedir() +
                                 "/.temp/" +
                                 (artifactCoordinates != null ? artifactCoordinates.toPath() : relativePath) +
                                 "." + temporaryTimestamp);

        // logger.debug("Creating temporary file '" + tempFile.getAbsolutePath() + "'...");

        return new ArtifactFile(tempFile);
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

        // logger.debug("Moving temporary file '" + getTemporaryFile().getAbsolutePath() + "' to '" + this.getAbsolutePath() + "'...");

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
