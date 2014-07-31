package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public class ArtifactInputStream extends MultipleDigestInputStream
{

    private Artifact artifact;


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

}
