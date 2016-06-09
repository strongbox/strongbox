package org.carlspring.strongbox.io;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.maven.commons.util.ArtifactUtils;

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

    private Artifact artifact;

    private long length = 0L;


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

    public ArtifactInputStream(Artifact artifact,
                               InputStream is)
            throws NoSuchAlgorithmException
    {
        super(is);
        this.artifact = artifact;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    public String getArtifactFileName()
    {
        return ArtifactUtils.getArtifactFileName(artifact);
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
