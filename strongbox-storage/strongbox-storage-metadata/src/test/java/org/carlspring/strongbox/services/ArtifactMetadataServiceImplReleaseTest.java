package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertNotNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceImplReleaseTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

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

            // Create released artifacts
            for (int i = 0; i <= 3; i++)
            {
                createRelease(ga + ":1." + i + ":jar");
            }

            // Testing scenario where 1.1 < 1.2 < 1.3 < 1.5 <  1.4
            // which might occur when 1.4 has been updated recently
            createRelease(ga + ":1.5:jar");

            // Delay generating this artifact to simulate it has been updated more recently than other versions.
            try {
                Thread.sleep(2000);
                artifact = createRelease(ga + ":1.4:jar");
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            // Create an artifact for metadata merging tests
            mergeArtifact = createRelease("org.carlspring.strongbox:strongbox-metadata-merge:1.0:jar");

            // Create plugin artifact
            pluginArtifact = createRelease("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.0:jar");
        }

        assertNotNull(basicRepositoryService);
    }

    @Test
    public void testReleaseMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", artifact);

        Metadata metadata = null;

        try
        {
            metadata = artifactMetadataService.getMetadata("storage0", "releases", artifact);
        }
        catch (IOException | XmlPullParserException e)
        {
            assertNotNull(metadata);
        }

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 6, versioning.getVersions().size());
    }

    @Test
    public void testReleasePluginMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", pluginArtifact);

        Metadata metadata = null;

        try
        {
            metadata = artifactMetadataService.getMetadata("storage0", "releases", pluginArtifact);
        }
        catch (IOException | XmlPullParserException e)
        {
            assertNotNull(metadata);
        }

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Incorrect latest release version!", pluginArtifact.getVersion(), versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException
    {
        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata("storage0", "releases", mergeArtifact);

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.1");
        appendVersioning.addVersion("1.2");

        appendVersioning.setRelease("1.2");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        artifactMetadataService.mergeMetadata("storage0", "releases", mergeArtifact, mergeMetadata);

        Metadata metadata = null;

        try
        {
            metadata = artifactMetadataService.getMetadata("storage0", "releases", mergeArtifact);
        }
        catch (IOException | XmlPullParserException e)
        {
            assertNotNull(metadata);
        }

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
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":source-release"));
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), ArtifactUtils.getArtifactFromGAVTC(gavt + ":sources"));

        return snapshot;
    }

    /**
     * Generate a released artifact.
     *
     * @param gavtc String
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
