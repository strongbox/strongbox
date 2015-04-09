package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i <= 2; i++)
            {
                calendar.add(Calendar.SECOND, 7);
                calendar.add(Calendar.MINUTE, 5);

                String timestamp = formatter.format(calendar.getTime());
                createSnapshot(ga + ":2.0-SNAPSHOT-" + timestamp + ":jar");
            }

            calendar.add(Calendar.SECOND, 7);
            calendar.add(Calendar.MINUTE, 5);
            String timestamp = formatter.format(calendar.getTime());

            artifact = createSnapshot(ga + ":2.0-SNAPSHOT-" + timestamp + ":jar");

            changeCreationDate(artifact);

            // Create an artifact for metadata merging tests
            mergeArtifact = createSnapshot("org.carlspring.strongbox:strongbox-metadata-merge:1.0:jar");

            // Create plugin artifact
            pluginArtifact = createSnapshot("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.1-SNAPSHOT:jar");
        }

        assertNotNull(basicRepositoryService);
    }

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
    public void testReleasePluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", pluginArtifact);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", pluginArtifact);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Incorrect latest release version!", pluginArtifact.getVersion(), versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }


    @Ignore
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", mergeArtifact);

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.1");
        appendVersioning.addVersion("1.2");

        appendVersioning.setRelease("1.2");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        artifactMetadataService.mergeMetadata("storage0", "snapshots", mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", mergeArtifact);

        assertNotNull(metadata);

        Assert.assertEquals("Incorrect latest release version!",mergeMetadata.getVersioning().getRelease(),metadata.getVersioning().getRelease());
        Assert.assertEquals("Incorrect number of versions stored in metadata!",
                            3,
                            metadata.getVersioning().getVersions().size());
    }

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param gavt String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Artifact createSnapshot(String gavt)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(REPOSITORY_BASEDIR.getAbsolutePath() + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), snapshot);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":javadoc"));
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                         ArtifactUtils.getArtifactFromGAVTC(gavt + ":source-release"));
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":sources"));

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
