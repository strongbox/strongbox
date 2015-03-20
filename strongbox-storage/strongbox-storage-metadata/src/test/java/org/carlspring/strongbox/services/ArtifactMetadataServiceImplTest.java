package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.assertNotNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceImplTest extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private Artifact artifact;
    private Artifact latestRelease;
    private Artifact latestSnapshot;

    private static final Artifact PLUGIN_ARTIFACT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.0:jar");
    private static final Artifact PLUGIN_ARTIFACT_SNAPSHOT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.1-SNAPSHOT:jar");

    private ArrayList<Artifact> testArtifacts = new ArrayList<>();

    @Autowired
    private ArtifactMetadataService artifactMetadataService;


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

            // Create released artifacts
            Artifact artifact = null;
            for (int i = 0; i <= 4; i++)
            {
                artifact = createRelease(ga + ":1." + i + ":jar");
            }

            this.artifact = artifact;
            latestRelease = artifact;

            // Create snapshot artifacts
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i <= 2; i++)
            {
                calendar.add(Calendar.SECOND, 7);
                calendar.add(Calendar.MINUTE, 5);

                String timestamp = formatter.format(calendar.getTime());
                Artifact snapshot = createSnapshot(ga + ":2.0-SNAPSHOT-" + timestamp + ":jar");

                if (i == 2)
                {
                    latestSnapshot = snapshot;
                }
            }

            // Create plugin artifact
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), PLUGIN_ARTIFACT);
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), PLUGIN_ARTIFACT_SNAPSHOT);
        }
    }

    @Test
    public void testMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", artifact);
    }

    @Test
    public void testPluginMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", PLUGIN_ARTIFACT);
    }

    @Ignore
    public void testMetadataRead()
            throws IOException, XmlPullParserException
    {
        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", artifact);
        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        //Assert.assertEquals("Incorrect latest version!", LATEST_ARTIFACT.getVersion(), versioning.getLatest());
        //Assert.assertEquals("Incorrect release version!", RELEASE_ARTIFACT.getVersion(), versioning.getRelease());
    }

    @Ignore
    public void testMetadataMerge()
            throws IOException, XmlPullParserException
    {
        Metadata mergeMetadata = new Metadata();
        Versioning versioning = new Versioning();

        versioning.addVersion("1.2");
        versioning.addVersion("1.3");

        SnapshotVersion snapshotVersion = new SnapshotVersion();
        snapshotVersion.setVersion("3.1.1-SNAPSHOT");

        versioning.addSnapshotVersion(snapshotVersion);
        versioning.setLatest("3.1.1-SNAPSHOT");

        mergeMetadata.setVersioning(versioning);

        artifactMetadataService.mergeMetadata("storage0", "releases", artifact, mergeMetadata);
    }

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param gavt      String
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
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":source-release"));
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":sources"));

        return snapshot;
    }

    /**
     * Generate a released artifact.
     *
     * @param gavtc     String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Artifact createRelease(String gavtc)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), gavtc);
    }

}
