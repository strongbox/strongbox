package org.carlspring.strongbox.testing;

import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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

    public void generateArtifact(String basedir, Artifact artifact, String packaging)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        artifact.setFile(new File(basedir + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(artifact, packaging);
    }

    public static void generateArtifact(String basedir, String gavtc, String... versions)
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
                                                      String baseSnapshotVersion)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 null,
                                                 1);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String baseSnapshotVersion,
                                                      int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 null,
                                                 numberOfBuilds);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String baseSnapshotVersion,
                                                      String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 classifiers,
                                                 1);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String baseSnapshotVersion,
                                                      String packaging,
                                                      String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 packaging,
                                                 classifiers,
                                                 1);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String baseSnapshotVersion,
                                                      String packaging,
                                                      String[] classifiers,
                                                      int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact artifact = null;

        for (int i = 0; i < numberOfBuilds; i++)
        {
            String version = createSnapshotVersion(baseSnapshotVersion, i + 1);

            artifact = new DetachedArtifact(groupId, artifactId, version);
            artifact.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

            generateArtifact(repositoryBasedir, artifact, packaging);

            if (classifiers != null)
            {
                for (String classifier : classifiers)
                {
                    String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                    generateArtifact(repositoryBasedir,ArtifactUtils.getArtifactFromGAVTC(gavtc));
                }
            }
        }

        // Return the main artifact
        return artifact;
    }

    public String createSnapshotVersion(String baseSnapshotVersion, int buildNumber)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, 7);
        calendar.add(Calendar.MINUTE, 5);

        String timestamp = formatter.format(calendar.getTime());
        @SuppressWarnings("UnnecessaryLocalVariable")
        String version = baseSnapshotVersion + "-" + timestamp + "-" + buildNumber;

        return version;
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
