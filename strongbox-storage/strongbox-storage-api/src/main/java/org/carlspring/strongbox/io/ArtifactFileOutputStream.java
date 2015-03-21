package org.carlspring.strongbox.io;

import org.apache.commons.io.FileUtils;

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


    public ArtifactFileOutputStream(ArtifactFile artifactFile)
            throws FileNotFoundException
    {
        super(artifactFile.isTemporaryMode() ? artifactFile.getTemporaryFile() : artifactFile);

        this.artifactFile = artifactFile;
    }

    @Override
    public void close()
            throws IOException
    {
        super.close();

        if (artifactFile.isTemporaryMode())
        {
            if (!artifactFile.getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                artifactFile.getParentFile().mkdirs();
            }

            moveTempFileToOriginalDestination();
        }
    }

    private void moveTempFileToOriginalDestination()
            throws IOException
    {
        FileUtils.moveFile(artifactFile.getTemporaryFile(), artifactFile);
    }

    public ArtifactFile getArtifactFile()
    {
        return artifactFile;
    }

    public void setArtifactFile(ArtifactFile artifactFile)
    {
        this.artifactFile = artifactFile;
    }

}
