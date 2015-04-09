package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceSnapshotsTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/snapshots");

    private static Artifact artifact;

    private static Artifact pluginArtifact;

    private static Artifact mergeArtifact;

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    @Autowired
    private BasicRepositoryService basicRepositoryService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private Calendar calendar = Calendar.getInstance();


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (!new File(REPOSITORY_BASEDIR, "org/carlspring/strongbox/strongbox-metadata").exists())
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR.mkdirs();

            //ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
            String ga = "org.carlspring.strongbox:strongbox-metadata";

            // Create snapshot artifacts

            int i = 0;
            for (; i <= 2; i++)
            {
                calendar.add(Calendar.SECOND, 7);
                calendar.add(Calendar.MINUTE, 5);

                String timestamp = formatter.format(calendar.getTime());
                createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                  "org.carlspring.strongbox",
                                                  "strongbox-metadata",
                                                  "2.0-" + timestamp + "-" + (i + 1));
            }

            calendar.add(Calendar.SECOND, 7);
            calendar.add(Calendar.MINUTE, 5);
            String timestamp = formatter.format(calendar.getTime());

            artifact = createSnapshot(REPOSITORY_BASEDIR.getAbsolutePath(), ga + ":2.0-" + timestamp + "-" + (i + 1) + ":jar");

            changeCreationDate(artifact);

            // Create an artifact for metadata merging tests
            mergeArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                              "org.carlspring.strongbox",
                                                              "strongbox-metadata-merge",
                                                              "2.0-" + formatter.format(calendar.getTime()) + "-1");

            // Create plugin artifact
            pluginArtifact = createSnapshot(REPOSITORY_BASEDIR.getAbsolutePath(),
                                            "org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.1-SNAPSHOT:jar");
        }

        assertNotNull(basicRepositoryService);
    }

    @Ignore
    @Test
    public void testSnapshotMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", artifact);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", artifact);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        //Assert.assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testSnapshotPluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", pluginArtifact);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", pluginArtifact);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        Assert.assertNull("Incorrect latest release version!", versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Ignore
    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", mergeArtifact);

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.0-SNAPSHOT");
        appendVersioning.addVersion("1.3-SNAPSHOT");

        Snapshot snapshot = new Snapshot();
        snapshot.setTimestamp(formatter.format(calendar.getTime()));
        snapshot.setBuildNumber(1);

        appendVersioning.setRelease(null);
        appendVersioning.setSnapshot(snapshot);
        appendVersioning.setLatest("1.3-SNAPSHOT");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        artifactMetadataService.mergeMetadata("storage0", "snapshots", mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", mergeArtifact);

        assertNotNull(metadata);

        Assert.assertEquals("Incorrect latest release version!", "1.3-SNAPSHOT", metadata.getVersioning().getLatest());
        // Assert.assertEquals("Incorrect latest release version!", null, metadata.getVersioning().getRelease());
        Assert.assertEquals("Incorrect number of versions stored in metadata!",
                            3,
                            metadata.getVersioning().getVersions().size());
    }

    private Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                       String groupId,
                                                       String artifactId,
                                                       String snapshotVersion/*,
                                                       String timestamp*/)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = new DetachedArtifact(groupId, artifactId, snapshotVersion);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);
        /*
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(ga + ":" + timestamp + ":jar:javadoc"));
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(ga + ":" + timestamp + ":jar:source-release"));
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(ga + ":" + timestamp + ":jar:sources"));
        */

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
    private Artifact createSnapshot(String repositoryBasedir, String gavt)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavt + ":javadoc"));
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavt + ":source-release"));
        generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavt + ":sources"));

        return snapshot;
    }

    private void changeCreationDate(Artifact artifact)
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
