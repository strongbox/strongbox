package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.RestAssuredTest;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.Headers;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RestAssuredTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MetadataManagementControllerTest
        extends RestAssuredBaseTest
{

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");
    private static final File REPOSITORY_BASEDIR_SNAPSHOTS = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/storage0/snapshots");

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    @BeforeClass
    public static void setUp()
            throws Exception
    {

        // remove release and snapshot directories
        removeDir(new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath()));
        removeDir(new File(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath()));

        // Generate releases
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.metadata:strongbox-metadata",
                         "3.0.1",
                         "3.0.2",
                         "3.1",
                         "3.2");

        // Generate snapshots
        TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();
        String snapshotPath = REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath();

        generator.createTimestampedSnapshotArtifact(snapshotPath, "org.carlspring.strongbox.metadata",
                                                    "strongbox-metadata", "3.0.1",
                                                    "jar",
                                                    null, 3);

        generator.createTimestampedSnapshotArtifact(snapshotPath, "org.carlspring.strongbox.metadata",
                                                    "strongbox-metadata", "3.0.2",
                                                    "jar",
                                                    null, 4);

        generator.createTimestampedSnapshotArtifact(snapshotPath, "org.carlspring.strongbox.metadata",
                                                    "strongbox-metadata", "3.1",
                                                    "jar",
                                                    null, 5);
    }

    @Test
    public void testRebuildSnapshotMetadata()
            throws Exception
    {
        String metadataPath = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        String url = getContextBaseUrl() + metadataPath;

        client.rebuildMetadata("storage0", "snapshots", null);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        InputStream is = client.getResource(metadataPath);
        Metadata metadata = artifactMetadataService.getMetadata(is);

        Assert.assertNotNull("Incorrect metadata!", metadata.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata.getVersioning().getLatest());
    }

    @Test
    public void testRebuildSnapshotMetadataWithBasePath()
            throws Exception
    {

        TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();
        // Generate snapshots in nested dirs
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata.foo", "strongbox-metadata-bar",
                                                    "1.2.3", "jar",
                                                    null, 5);
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata.foo.bar",
                                                    "strongbox-metadata-foo", "2.1", "jar",
                                                    null, 5);
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata.foo.bar",
                                                    "strongbox-metadata-foo-bar", "5.4", "jar",
                                                    null, 4);

        String metadataUrl = "/storages/storage0/snapshots/org/carlspring/strongbox/metadata";
        String metadataPath1 = metadataUrl + "/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = metadataUrl + "/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
        String metadataPath2Snapshot = metadataUrl + "/foo/bar/strongbox-metadata-foo/2.1-SNAPSHOT/maven-metadata.xml";
        String metadataPath3 = metadataUrl + "/foo/bar/strongbox-metadata-foo-bar/maven-metadata.xml";

        Assert.assertFalse("Metadata already exists!", client.pathExists(metadataPath1));
        Assert.assertFalse("Metadata already exists!", client.pathExists(metadataPath2));
        Assert.assertFalse("Metadata already exists!", client.pathExists(metadataPath3));

        client.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/metadata/foo/bar");

        Assert.assertFalse("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath1));
        assertTrue("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath2));
        assertTrue("Failed to rebuild snapshot metadata!", client.pathExists(metadataPath3));

        InputStream is = client.getResource(metadataPath2);
        Metadata metadata2 = artifactMetadataService.getMetadata(is);

        Assert.assertNotNull("Incorrect metadata!", metadata2.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata2.getVersioning().getLatest());

        is = client.getResource(metadataPath3);
        Metadata metadata3 = artifactMetadataService.getMetadata(is);

        Assert.assertNotNull("Incorrect metadata!", metadata3.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata3.getVersioning().getLatest());

        // Test the deletion of a timestamped SNAPSHOT artifact
        is = client.getResource(metadataPath2Snapshot);
        Metadata metadata2SnapshotBefore = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2SnapshotVersions = metadata2SnapshotBefore.getVersioning().getSnapshotVersions();
        // This is minus three because in this case there are no classifiers, there's just a pom and a jar,
        // thus two and therefore getting the element before them would be three:
        String previousLatestTimestamp = metadata2SnapshotVersions.get(
                metadata2SnapshotVersions.size() - 3).getVersion();
        String latestTimestamp = metadata2SnapshotVersions.get(metadata2SnapshotVersions.size() - 1).getVersion();

        logger.info("[testRebuildSnapshotMetadataWithBasePath] latestTimestamp " + latestTimestamp);

        client.removeVersionFromMetadata("storage0",
                                         "snapshots",
                                         "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo",
                                         latestTimestamp,
                                         null,
                                         MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        is = client.getResource(metadataPath2Snapshot);
        Metadata metadata2SnapshotAfter = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2AfterSnapshotVersions = metadata2SnapshotAfter.getVersioning().getSnapshotVersions();

        String timestamp = previousLatestTimestamp.substring(previousLatestTimestamp.indexOf('-') + 1,
                                                             previousLatestTimestamp.lastIndexOf('-'));
        String buildNumber = previousLatestTimestamp.substring(previousLatestTimestamp.lastIndexOf('-') + 1,
                                                               previousLatestTimestamp.length());

        logger.info("\n\tpreviousLatestTimestamp " + previousLatestTimestamp + "\n\ttimestamp " + timestamp +
                    "\n\tbuildNumber " + buildNumber);

        Assert.assertNotNull("Incorrect metadata!", metadata2SnapshotAfter.getVersioning());
        Assert.assertFalse("Failed to remove timestamped SNAPSHOT version!",
                           MetadataHelper.containsVersion(metadata2SnapshotAfter, latestTimestamp));
        Assert.assertEquals("Incorrect metadata!", timestamp,
                            metadata2SnapshotAfter.getVersioning().getSnapshot().getTimestamp());
        Assert.assertEquals("Incorrect metadata!", Integer.parseInt(buildNumber),
                            metadata2SnapshotAfter.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertEquals("Incorrect metadata!", previousLatestTimestamp,
                            metadata2AfterSnapshotVersions.get(metadata2AfterSnapshotVersions.size() - 1).getVersion());
    }


    private void rebuildReleaseMetadataAndDeleteAVersion()
            throws Exception
    {

        // define bla bla bla
        String metadataPath = "/storages/storage0/releases";
        String path = "org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";
        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        String url = getContextBaseUrl() + (metadataPath.startsWith("/") ? metadataPath : '/' + metadataPath);
        url += "/" + path;

        logger.debug("Path to artifact: " + url);

        // assert that metadata don't exists (404)
        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        // create new metadata
        client.rebuildMetadata("storage0", "releases", artifactPath);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(200);

        InputStream is = getArtifactAsStream(path, metadataPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning());
        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.2", metadataBefore.getVersioning().getLatest());

        url = getContextBaseUrl() + "/metadata/" +
              "storage0" + "/" + "releases";

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", artifactPath, "version", "3.2", "classifier", "", "metadataType",
                        MetadataType.ARTIFACT_ROOT_LEVEL.getType())
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);

        is = getArtifactAsStream(path, metadataPath);
        Metadata metadataAfter = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning());
        assertFalse("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "3.2"));
        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.1", metadataAfter.getVersioning().getLatest());
    }

    private InputStream getArtifactAsStream(String path,
                                            String url)
    {
        return getArtifactAsStream(path, url, -1);
    }

    private InputStream getArtifactAsStream(String path,
                                            String url,
                                            int offset)
    {
        return new ByteArrayInputStream(getArtifactAsByteArray(path, url, offset));

    }

    private byte[] getArtifactAsByteArray(String path,
                                          String url,
                                          int offset)
    {
        MockMvcRequestSpecification o = given().contentType(MediaType.TEXT_PLAIN_VALUE);
        int statusCode = 200;
        if (offset != -1)
        {
            o = o.header("Range", "bytes=" + offset + "-");
            statusCode = 206;
        }

        MockMvcResponse response = o.param("path", path)
                                    .when()
                                    .get(url);
        Headers allHeaders = response.getHeaders();

        logger.debug("HTTP GET " + url);
        logger.debug("Response headers:");

        allHeaders.forEach(header ->
                           {
                               logger.debug("\t" + header.getName() + " = " + header.getValue());
                           });

        response.then().statusCode(statusCode);
        byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();

        logger.debug("Received " + result.length + " bytes.");

        return result;
    }
}
