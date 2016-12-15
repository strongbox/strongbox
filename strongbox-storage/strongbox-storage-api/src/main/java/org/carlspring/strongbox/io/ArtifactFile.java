package org.carlspring.strongbox.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(ArtifactFile.class);

    private ArtifactCoordinates artifactCoordinates;

    private long temporaryTimestamp = System.nanoTime();

    private boolean temporaryMode;

    private String baseDir;

    public ArtifactFile(String baseDir,
                        ArtifactCoordinates artifactCoordinates)
    {
        this(baseDir, artifactCoordinates, false);
    }

    public ArtifactFile(String baseDir,
                        ArtifactCoordinates artifactCoordinates,
                        boolean temporaryMode)
    {
        this(baseDir + File.separator + artifactCoordinates.toPath(), baseDir, artifactCoordinates, temporaryMode);
    }

    ArtifactFile(String absolutePath,
                 String baseDir,
                 ArtifactCoordinates artifactCoordinates,
                 boolean temporaryMode)
    {
        super(absolutePath);

        this.baseDir = baseDir;
        this.artifactCoordinates = artifactCoordinates;
        this.temporaryMode = temporaryMode;
    }

    public ArtifactFile getTemporaryFile()
    {
        String absolutePath = baseDir +
                File.separator + ".temp" + File.separator +
                artifactCoordinates.toPath() +
                "." + temporaryTimestamp;
        return new ArtifactFile(absolutePath, artifactCoordinates, false);
    }

    public void createParents()
    {
        if (isTemporaryMode())
        {
            if (!getTemporaryFile().getParentFile().exists())
            {
                // noinspection ResultOfMethodCallIgnored
                getTemporaryFile().getParentFile().mkdirs();
            }
        } else
        {
            if (!getParentFile().exists())
            {
                // noinspection ResultOfMethodCallIgnored
                getParentFile().mkdirs();
            }
        }
    }

    public void moveTempFileToOriginalDestination()
        throws IOException
    {
        if (!getParentFile().exists())
        {
            // noinspection ResultOfMethodCallIgnored
            getParentFile().mkdirs();
        }

        if (this.exists())
        {
            FileUtils.forceDelete(this);
        }

        // logger.debug("Moving temporary file '" + getTemporaryFile().getAbsolutePath() + "' to '" +
        // this.getAbsolutePath() + "'...");

        FileUtils.moveFile(getTemporaryFile(), this);
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

    public FileOutputStream getOutputStream(boolean moveOnClose)
        throws IOException
    {
        FileOutputStream os = new FileOutputStream(isTemporaryMode() ? getTemporaryFile() : this)
        {

            @Override
            public void close()
                throws IOException
            {
                if (ArtifactFile.this.isTemporaryMode() && moveOnClose)
                {
                    ArtifactFile.this.moveTempFileToOriginalDestination();
                }
                super.close();
            }

        };
        return os;
    }

}
