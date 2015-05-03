package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
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
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceReleasesTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

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
            artifact = createRelease(ga + ":1.4:jar");

            changeCreationDate(artifact);

            // Create an artifact for metadata merging tests
            mergeArtifact = createRelease("org.carlspring.strongbox:strongbox-metadata-merge:1.0:jar");

            // Create plugin artifact
            pluginArtifact = createRelease("org.carlspring.strongbox.maven:strongbox-metadata-plugin:1.0:jar");
        }

        assertNotNull(basicRepositoryService);
    }

    @Test
    public void testReleaseMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", artifact);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", artifact);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        // TODO: Fix this as part of SB-333:
        //Assert.assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());
        Assert.assertEquals("Incorrect latest release version!", "1.5", versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 6, versioning.getVersions().size());
    }

    @Test
    public void testReleasePluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "releases", pluginArtifact);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", pluginArtifact);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Incorrect latest release version!", pluginArtifact.getVersion(), versioning.getRelease());

        Assert.assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
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

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", mergeArtifact);

        assertNotNull(metadata);

        Assert.assertEquals("Incorrect latest release version!",
                            mergeMetadata.getVersioning().getRelease(),
                            metadata.getVersioning().getRelease());
        Assert.assertEquals("Incorrect number of versions stored in metadata!",
                            3,
                            metadata.getVersioning().getVersions().size());
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
