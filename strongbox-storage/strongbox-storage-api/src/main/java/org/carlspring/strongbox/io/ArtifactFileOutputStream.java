package org.carlspring.strongbox.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author mtodorov
 */
public class ArtifactFileOutputStream
        extends FileOutputStream
{

    private ArtifactFile artifactFile;

    private boolean moveOnClose;


    public ArtifactFileOutputStream(ArtifactFile artifactFile)
            throws FileNotFoundException
    {
        this(artifactFile, true);
    }

    public ArtifactFileOutputStream(ArtifactFile artifactFile, boolean moveOnClose)
            throws FileNotFoundException
    {
        super(artifactFile.isTemporaryMode() ? artifactFile.getTemporaryFile() : artifactFile);

        this.artifactFile = artifactFile;
        this.moveOnClose = moveOnClose;
    }

    @Override
    public void close()
            throws IOException
    {
        super.close();

        if (artifactFile.isTemporaryMode() && moveOnClose)
        {
            artifactFile.moveTempFileToOriginalDestination();
        }
    }

    public ArtifactFile getArtifactFile()
    {
        return artifactFile;
    }

    public void setArtifactFile(ArtifactFile artifactFile)
    {
        this.artifactFile = artifactFile;
    }

    public boolean isMoveOnClose()
    {
        return moveOnClose;
    }

    public void setMoveOnClose(boolean moveOnClose)
    {
        this.moveOnClose = moveOnClose;
    }

}
