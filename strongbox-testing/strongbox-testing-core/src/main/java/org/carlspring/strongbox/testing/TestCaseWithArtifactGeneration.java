package org.carlspring.strongbox.testing;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
public class TestCaseWithArtifactGeneration
{

    public Artifact generateArtifact(String basedir, String gavtc)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        artifact.setFile(new File(basedir + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

        generateArtifact(basedir, artifact);

        return artifact;
    }

    public void generateArtifact(String basedir, Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        artifact.setFile(new File(basedir + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(artifact);
    }

    public void generateArtifact(String basedir, String gavtc, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(gavtc, versions);
    }

    public InputStream generateArtifactInputStream(String basedir, String repositoryId, String gavtc, boolean useTempDir)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        File baseDir = new File(basedir + "/" + repositoryId + (useTempDir ? "/.temp" : ""));
        if (!baseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }

        Artifact artifact = generateArtifact(baseDir.getCanonicalPath(), gavtc);

        return new FileInputStream(new File(baseDir, ArtifactUtils.convertArtifactToPath(artifact)));
    }

}
