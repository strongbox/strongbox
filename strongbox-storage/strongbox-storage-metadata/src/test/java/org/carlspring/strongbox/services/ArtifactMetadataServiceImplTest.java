package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.services.impl.ArtifactMetadataServiceImpl;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.JVM)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceImplTest {

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private static Artifact ARTIFACT = null;
    private static Artifact LATEST_RELEASE = null;
    private static Artifact LATEST_SNAPSHOT = null;

    private static final Artifact PLUGIN_ARTIFACT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.0:jar");
    private static final Artifact PLUGIN_ARTIFACT_SNAPSHOT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.1-SNAPSHOT:jar");

    private ArrayList<Artifact> testArtifacts = new ArrayList<>();

    @Autowired
    private ArtifactMetadataServiceImpl artifactMetadataService;

    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException {
        if (!new File(REPOSITORY_BASEDIR, "org/carlspring/strongbox/strongbox-metadata").exists()) {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR.mkdirs();

            ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
            String ga = "org.carlspring.strongbox:strongbox-metadata";

            // Create released artifacts
            for(int i = 0; i <= 4; i++)
            {
                Artifact artifact = createRelease(generator, ga + ":1." + i + ":jar");

                if(i == 4)
                {
                    ARTIFACT = artifact;
                    LATEST_RELEASE = artifact;
                }
            }


            // Create snapshot artifacts
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar calendar = Calendar.getInstance();

            for(int i = 0; i <= 2; i++)
            {
                calendar.add(Calendar.SECOND, 7);
                calendar.add(Calendar.MINUTE, 5);

                String timestamp = formatter.format(calendar.getTime());
                Artifact snapshot = createSnapshot(generator, ga + ":2.0-SNAPSHOT-" + timestamp + ":jar");

                if(i == 2)
                {
                    LATEST_SNAPSHOT = snapshot;
                }
            }

            // Create plugin artifact
            generator.generate(PLUGIN_ARTIFACT);
            PLUGIN_ARTIFACT.setFile(new File(REPOSITORY_BASEDIR.getAbsolutePath() + "/" + ArtifactUtils.convertArtifactToPath(PLUGIN_ARTIFACT)));

            generator.generate(PLUGIN_ARTIFACT_SNAPSHOT);
            PLUGIN_ARTIFACT_SNAPSHOT.setFile(new File(REPOSITORY_BASEDIR.getAbsolutePath() + "/" + ArtifactUtils.convertArtifactToPath(PLUGIN_ARTIFACT_SNAPSHOT)));
        }
    }

    @Test
    public void testMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", ARTIFACT);
    }

    @Test
    public void testPluginMetadataRebuild()
            throws IOException, XmlPullParserException {
        artifactMetadataService.rebuildMetadata("storage0", "releases", PLUGIN_ARTIFACT);
    }

    @Ignore
    public void testMetadataRead()
            throws IOException, XmlPullParserException
    {
        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", ARTIFACT);
        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", ARTIFACT.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", ARTIFACT.getGroupId(), metadata.getGroupId());
        //Assert.assertEquals("Incorrect latest version!", LATEST_ARTIFACT.getVersion(), versioning.getLatest());
        //Assert.assertEquals("Incorrect release version!", RELEASE_ARTIFACT.getVersion(), versioning.getRelease());
    }

    @Ignore
    public void testMetadataMerge()
            throws IOException, XmlPullParserException {
        Metadata mergeMetadata = new Metadata();
        Versioning versioning = new Versioning();

        versioning.addVersion("1.2");
        versioning.addVersion("1.3");

        SnapshotVersion snapshotVersion = new SnapshotVersion();
        snapshotVersion.setVersion("3.1.1-SNAPSHOT");

        versioning.addSnapshotVersion(snapshotVersion);

        versioning.setLatest("3.1.1-SNAPSHOT");

        mergeMetadata.setVersioning(versioning);

        artifactMetadataService.mergeMetadata("storage0", "releases", ARTIFACT, mergeMetadata);
    }


    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param generator  ArtifactGenerator
     * @param gavt        String
     *
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Artifact createSnapshot(ArtifactGenerator generator, String gavt)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {

        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        generator.generate(snapshot);

        // The generator doesn't populate properly everything in the artifact object, so we need to make sure it's set manually.
        snapshot.setFile(new File(REPOSITORY_BASEDIR.getAbsolutePath() + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        Artifact snapshotJavadoc = ArtifactUtils.getArtifactFromGAVTC(gavt + ":javadoc");
        generator.generate(snapshotJavadoc);

        Artifact snapshotSourceRelease = ArtifactUtils.getArtifactFromGAVTC(gavt + ":source-release");
        generator.generate(snapshotSourceRelease);

        Artifact snapshotSources = ArtifactUtils.getArtifactFromGAVTC(gavt + ":sources");
        generator.generate(snapshotSources);

        return snapshot;
    }

    /**
     * Generate a released artifact.
     *
     * @param generator ArtifactGenerator
     * @param gavtc     String
     *
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Artifact createRelease(ArtifactGenerator generator, String gavtc)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        generator.generate(artifact);

        // The generator doesn't populate properly everything in the artifact object, so we need to make sure it's set manually.
        artifact.setFile(new File(REPOSITORY_BASEDIR.getAbsolutePath() + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

        return artifact;
    }

}
