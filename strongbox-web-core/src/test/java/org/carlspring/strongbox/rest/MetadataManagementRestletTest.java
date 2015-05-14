package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            // Generate releases
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "org.carlspring.strongbox.metadata:strongbox-metadata", new String[]{"3.0.1", "3.0.2", "3.1"});

            // Generate snapshots
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.1", 3);
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.2", 4);
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.1", 5);

            INITIALIZED = true;
        }
    }

    @Test
    public void testRebuildReleaseMetadata()
            throws Exception
    {
        String metadataPath = "/storages/storage0/releases/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        int response = client.rebuildMetadata("storage0", "releases", null);

        System.out.println(response);

        assertEquals("Received unexpected response!", 200, response);
        assertTrue("Failed to rebuild release metadata!", client.pathExists(metadataPath));
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
    }

    @Test
    public void testRebuildSnapshotMetadataWithBasePath()
            throws Exception
    {
        // Generate snapshots in nested dirs
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo", "strongbox-metadata-bar", "1.2.3", 5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo.bar", "strongbox-metadata-foo", "2.1", 3);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(), "org.carlspring.strongbox.metadata.foo.bar", "strongbox-metadata-foo-bar", "5.4", 4);

        String metadataPath1 = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
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
    }

}
