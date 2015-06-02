package org.carlspring.strongbox.rest;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class MetadataManagementRestletTest
    extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");
    private static final File REPOSITORY_BASEDIR_SNAPSHOTS = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/snapshots");

    public static boolean INITIALIZED = false;

    private static RestClient client = new RestClient();

    @Autowired
    private ArtifactMetadataService artifactMetadataService;


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            // Generate releases
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "org.carlspring.strongbox.metadata:strongbox-metadata", new String[]{"3.0.1", "3.0.2", "3.1", "3.2"});

            // Generate snapshots
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.1", 3);
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.2", 4);
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.1", 5);

            INITIALIZED = true;
        }
    }

    @Test
    public void testRebuildReleaseMetadataAndDeleteAVersion()
            throws Exception
    {
        String metadataPath = "/storages/storage0/releases/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        int response = client.rebuildMetadata("storage0", "releases", null);

        System.out.println(response);

        assertEquals("Received unexpected response!", 200, response);
        assertTrue("Failed to rebuild release metadata!", client.pathExists(metadataPath));

        InputStream is = client.getResource(metadataPath);
        Metadata metadataBefore = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning());
        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.2", metadataBefore.getVersioning().getLatest());

        response = client.removeVersionFromMetadata("storage0", "releases", artifactPath, "3.2", null, MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        assertEquals("Received unexpected response!", 200, response);

        is = client.getResource(metadataPath);
        Metadata metadataAfter = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning());
        assertFalse("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "3.2"));
        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.1", metadataAfter.getVersioning().getLatest());
    }

    @Test
    public void testRebuildSnapshotMetadata()
            throws Exception
    {
        String metadataPath = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        int response = client.rebuildMetadata("storage0", "snapshots", null);

        System.out.println(response);

        assertEquals("Received unexpected response!", 200, response);
        assertTrue("Failed to rebuild snapshots metadata!", client.pathExists(metadataPath));

        InputStream is = client.getResource(metadataPath);
        Metadata metadata = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadata.getVersioning());
        assertNotNull("Incorrect metadata!", metadata.getVersioning().getLatest());
    }

    @Test
    public void testRebuildSnapshotMetadataWithBasePath()
            throws Exception
    {
        // Generate snapshots in nested dirs
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo", "strongbox-metadata-bar", "1.2.3", 5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo.bar", "strongbox-metadata-foo", "2.1", 5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo.bar", "strongbox-metadata-foo-bar", "5.4", 4);

        String metadataPath1 = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
        String metadataPath2Snapshot = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo/2.1-SNAPSHOT/maven-metadata.xml";
        String metadataPath3 = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo-bar/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath1));
        assertFalse("Metadata already exists!", client.pathExists(metadataPath2));
        assertFalse("Metadata already exists!", client.pathExists(metadataPath3));

        int response = client.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/metadata/foo/bar");

        System.out.println(response);

        assertEquals("Received unexpected response!", 200, response);
        assertFalse("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath1));
        assertTrue("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath2));
        assertTrue("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath3));

        InputStream is = client.getResource(metadataPath2);
        Metadata metadata2 = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadata2.getVersioning());
        assertNotNull("Incorrect metadata!", metadata2.getVersioning().getLatest());

        is = client.getResource(metadataPath3);
        Metadata metadata3 = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadata3.getVersioning());
        assertNotNull("Incorrect metadata!", metadata3.getVersioning().getLatest());

        // Test the deletion of a timestamped SNAPSHOT artifact
        is = client.getResource(metadataPath2Snapshot);
        Metadata metadata2SnapshotBefore = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2SnapshotVersions = metadata2SnapshotBefore.getVersioning().getSnapshotVersions();
        // This is minus three because in this case there are no classifiers, there's just a pom and a jar,
        // thus two and therefore getting the element before them would be three:
        String previousLatestTimestamp = metadata2SnapshotVersions.get(metadata2SnapshotVersions.size() - 3).getVersion();
        String latestTimestamp = metadata2SnapshotVersions.get(metadata2SnapshotVersions.size() - 1).getVersion();

        response = client.removeVersionFromMetadata("storage0",
                                                    "snapshots",
                                                    "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo",
                                                    latestTimestamp,
                                                    null,
                                                    MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        assertEquals("Received unexpected response!", 200, response);

        is = client.getResource(metadataPath2Snapshot);
        Metadata metadata2SnapshotAfter = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2AfterSnapshotVersions = metadata2SnapshotAfter.getVersioning().getSnapshotVersions();

        assertNotNull("Incorrect metadata!", metadata2SnapshotAfter.getVersioning());
        assertFalse("Failed to remove timestamped SNAPSHOT version!", MetadataHelper.containsVersion(metadata2SnapshotAfter, latestTimestamp));
        // assertNotNull("Incorrect metadata!", metadata2SnapshotAfter.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", previousLatestTimestamp, metadata2AfterSnapshotVersions.get(metadata2AfterSnapshotVersions.size() - 1).getVersion());
    }

}
