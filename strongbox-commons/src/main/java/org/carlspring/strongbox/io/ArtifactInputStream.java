package org.carlspring.strongbox.io;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.http.range.ByteRange;
import org.carlspring.strongbox.io.reloading.ReloadableInputStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

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
