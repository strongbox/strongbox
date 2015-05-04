package org.carlspring.strongbox.testing;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

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

    public void generateArtifact(File basedir, Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        artifact.setFile(new File(basedir + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(artifact);
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

    public void generateArtifact(String basedir, String gavtc, String packaging, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(gavtc, packaging, versions);
    }

    public void generatePluginArtifact(String basedir, String gavtc, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(gavtc, "maven-plugin", versions);
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

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String snapshotVersion,
                                                      String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = new DetachedArtifact(groupId, artifactId, snapshotVersion);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);

        if (classifiers != null)
        {
            for (String classifier : classifiers)
            {
                generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(groupId + ":" + artifactId + ":" + snapshotVersion + ":jar:" + classifier));
            }
        }

        return snapshot;
    }

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param repositoryBasedir String
     * @param gavt String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    public Artifact createSnapshot(String repositoryBasedir, String gavt)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);

        return snapshot;
    }

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param repositoryBasedir String
     * @param gavt String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    public Artifact createSnapshot(String repositoryBasedir, String gavt, String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);

        for (String classifier : classifiers)
        {
            generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavt + ":" + classifier));

        }

        return snapshot;
    }

    public void changeCreationDate(Artifact artifact)
            throws IOException
    {
        File directory = artifact.getFile().toPath().getParent().toFile();

        //noinspection ConstantConditions
        for (final File fileEntry : directory.listFiles())
        {
            if (fileEntry.isFile())
            {
                BasicFileAttributeView attributes = Files.getFileAttributeView(fileEntry.toPath(), BasicFileAttributeView.class);
                FileTime time = FileTime.from(System.currentTimeMillis() + 60000L, TimeUnit.MILLISECONDS);
                attributes.setTimes(time, time, time);
            }
        }
    }

}
