package org.carlspring.strongbox.rest;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

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

        InputStream is = client.getResource(metadataPath);
        Metadata metadata = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadata.getVersioning());
        assertNotNull("Incorrect metadata!", metadata.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.1", metadata.getVersioning().getLatest());
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

        Response resourceWithResponse = client.getResourceWithResponse(metadataPath2);
        InputStream is = resourceWithResponse.readEntity(InputStream.class);
        Metadata metadata2 = artifactMetadataService.getMetadata(is);

        String md5 = resourceWithResponse.getHeaderString("Checksum-MD5");
        String sha1 = resourceWithResponse.getHeaderString("Checksum-SHA1");

        assertNotNull("Failed to retrieve MD5 checksum via HTTP header!", md5);
        assertNotNull("Failed to retrieve SHA-1 checksum via HTTP header!", sha1);

        System.out.println("MD5:   " + md5);
        System.out.println("SHA-1: " + sha1);

        assertNotNull("Incorrect metadata!", metadata2.getVersioning());
        assertNotNull("Incorrect metadata!", metadata2.getVersioning().getLatest());

        is = client.getResource(metadataPath3);
        Metadata metadata3 = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadata3.getVersioning());
        assertNotNull("Incorrect metadata!", metadata3.getVersioning().getLatest());
    }

}
