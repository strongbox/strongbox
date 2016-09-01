package org.carlspring.strongbox.io;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public class ArtifactInputStream extends MultipleDigestInputStream
{

    private ArtifactCoordinates artifactCoordinates;

    private long length;


    public ArtifactInputStream(ReloadableInputStreamHandler handler, List<ByteRange> byteRanges)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler, byteRanges);
    }

    public ArtifactInputStream(ReloadableInputStreamHandler handler, ByteRange byteRange)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler, byteRange);
    }

    public ArtifactInputStream(InputStream is)
            throws NoSuchAlgorithmException
    {
        super(is);
    }
    
    public ArtifactInputStream(InputStream is,
                               String[] algorithms)
            throws NoSuchAlgorithmException
    {
        super(is, algorithms);
    }

    public ArtifactInputStream(ArtifactCoordinates artifactCoordinates,
                               InputStream is)
            throws NoSuchAlgorithmException
    {
        super(is);
        this.artifactCoordinates = artifactCoordinates;
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

}
