package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.Headers;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * Created by yury on 8/25/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class MetadataManagementControllerTest
        extends BackendBaseTest
{

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");
    private static final File REPOSITORY_BASEDIR_SNAPSHOTS = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/storage0/snapshots");
    private static final Logger logger = LoggerFactory.getLogger(MetadataManagementControllerTest.class);

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    @Before
    public void setUp()
            throws Exception
    {

        // remove release directory
        removeDir(new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath()));
        removeDir(new File(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath()));

        logger.debug("Generate releases...");

        // Generate releases
        TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();
        TestCaseWithArtifactGeneration.generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                                                        "org.carlspring.strongbox.metadata:strongbox-metadata",
                                                        new String[]{ "3.0.1",
                                                                      "3.0.2",
                                                                      "3.1",
                                                                      "3.2" });

        // Generate snapshots
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.1",
                                                    "jar",
                                                    null, 3);
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.0.2",
                                                    "jar",
                                                    null, 4);
        generator.createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                                    "org.carlspring.strongbox.metadata", "strongbox-metadata", "3.1",
                                                    "jar",
                                                    null, 5);

    }

    private void removeDir(File dir)
    {

        if (dir == null)
        {
            return;
        }

        System.out.println("Removing directory " + dir.getAbsolutePath());

        if (dir.isDirectory())
        {
            File[] files = dir.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    removeDir(file);
                }
            }
        }
        else
        {
            boolean res = dir.delete();
            System.out.println("Remove " + dir.getAbsolutePath() + " " + res);
        }
    }

    @Test
    public void testMetadata()
            throws Exception
    {
        rebuildSnapshotMetadata();
        rebuildSnapshotMetadataWithBasePath();
        rebuildReleaseMetadataAndDeleteAVersion();
    }

    private void rebuildReleaseMetadataAndDeleteAVersion()
            throws Exception
    {

        // define bla bla bla
        String metadataPath = "/storages/storage0/releases";
        String path = "org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";
        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        String url = getContextBaseUrl() + (metadataPath.startsWith("/") ? metadataPath : '/' + metadataPath);
        logger.debug("Path to artifact: " + url);

        // assert that metadata don't exists (404)
        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        // create new metadata
        rebuildMetadata("storage0", "releases", artifactPath);

        url = getContextBaseUrl() + (metadataPath.startsWith("/") ? metadataPath : '/' + metadataPath);

        logger.debug("Path to artifact: " + url);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
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
        System.out.println("HTTP GET " + url);
        System.out.println("Response headers:");
        allHeaders.forEach(header ->
                           {
                               System.out.println("\t" + header.getName() + " = " + header.getValue());
                           });

        response.then().statusCode(statusCode);
        byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();
        System.out.println("Received " + result.length + " bytes.");

        return result;
    }

    private void rebuildSnapshotMetadata()
            throws Exception
    {
        String metadataPath = "/storages/storage0/snapshots";
        String path = "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        String url = getContextBaseUrl() + (metadataPath.startsWith("/") ? metadataPath : '/' + metadataPath);
        logger.debug("Path to artifact: " + url);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(404);

        rebuildMetadata("storage0", "snapshots", null);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
                .when()
                .get(url)
                .peek()
                .then()
                .statusCode(200);

        InputStream is = getArtifactAsStream(path, metadataPath);
        Metadata metadata = artifactMetadataService.getMetadata(is);

        Assert.assertNotNull("Incorrect metadata!", metadata.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata.getVersioning().getLatest());
    }

    private void rebuildSnapshotMetadataWithBasePath()
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

        String metadataUrl = "/storages/storage0/snapshots";
        String metadataPath1 = "/org/carlspring/strongbox/metadata/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = "/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
        String metadataPath2Snapshot = "/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo/2.1-SNAPSHOT/maven-metadata.xml";
        String metadataPath3 = "/org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo-bar/maven-metadata.xml";

        pathExists(metadataPath1, metadataUrl, false);
        pathExists(metadataPath2, metadataUrl, false);
        pathExists(metadataPath3, metadataUrl, false);

        rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/metadata/foo/bar");

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", "org/carlspring/strongbox/metadata/foo/bar")
                .when()
                .post(getContextBaseUrl() + "/metadata/" + "storage0" + "/" + "snapshots")
                .peek()
                .then()
                .statusCode(200);

        pathExists(metadataPath1, metadataUrl, false);
        pathExists(metadataPath2, metadataUrl, true);
        pathExists(metadataPath3, metadataUrl, true);

        String url = getContextBaseUrl() + (!metadataUrl.startsWith("/") ? "/" : "") + metadataUrl;

        logger.debug("Getting " + url + "...");

        MockMvcResponse response = given()
                                           .contentType(MediaType.TEXT_PLAIN_VALUE)
                                           .param("path", metadataPath2)
                                           .when()
                                           .get(url);

        Headers allHeaders = response.getHeaders();
        System.out.println("HTTP GET " + url);
        System.out.println("Response headers:");
        allHeaders.forEach(header ->
                           {
                               System.out.println("\t" + header.getName() + " = " + header.getValue());
                           });

        response.then().statusCode(200);
        byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();
        System.out.println("Received " + result.length + " bytes.");

        InputStream is = new ByteArrayInputStream(result);
        Metadata metadata2 = artifactMetadataService.getMetadata(is);

        String md5 = response.getHeader("Checksum-MD5");
        String sha1 = response.getHeader("Checksum-SHA1");

        Assert.assertNotNull("Failed to retrieve MD5 checksum via HTTP header!", md5);
        Assert.assertNotNull("Failed to retrieve SHA-1 checksum via HTTP header!", sha1);

        System.out.println("MD5:   " + md5);
        System.out.println("SHA-1: " + sha1);

        Assert.assertNotNull("Incorrect metadata!", metadata2.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata2.getVersioning().getLatest());

        is = getArtifactAsStream(metadataPath3, metadataUrl);
        Metadata metadata3 = artifactMetadataService.getMetadata(is);

        Assert.assertNotNull("Incorrect metadata!", metadata3.getVersioning());
        Assert.assertNotNull("Incorrect metadata!", metadata3.getVersioning().getLatest());

        // Test the deletion of a timestamped SNAPSHOT artifact
        is = getArtifactAsStream(metadataPath2Snapshot, metadataUrl);
        Metadata metadata2SnapshotBefore = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2SnapshotVersions = metadata2SnapshotBefore.getVersioning().getSnapshotVersions();
        // This is minus three because in this case there are no classifiers, there's just a pom and a jar,
        // thus two and therefore getting the element before them would be three:
        String previousLatestTimestamp = metadata2SnapshotVersions.get(
                metadata2SnapshotVersions.size() - 3).getVersion();
        String latestTimestamp = metadata2SnapshotVersions.get(metadata2SnapshotVersions.size() - 1).getVersion();

     /*   response = client.removeVersionFromMetadata("storage0",
                                                    "snapshots",
                                                    "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo",
                                                    latestTimestamp,
                                                    null,
                                                    MetadataType.ARTIFACT_ROOT_LEVEL.getType());*/

        url = getContextBaseUrl() + "/metadata/" +
              "storage0" + "/" + "snapshots";

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo", "version",
                        latestTimestamp, "classifier", "", "metadataType", MetadataType.ARTIFACT_ROOT_LEVEL.getType())
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);

        is = getArtifactAsStream(metadataPath2Snapshot, metadataUrl);
        Metadata metadata2SnapshotAfter = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2AfterSnapshotVersions = metadata2SnapshotAfter.getVersioning().getSnapshotVersions();

        String timestamp = previousLatestTimestamp.substring(previousLatestTimestamp.indexOf('-') + 1,
                                                             previousLatestTimestamp.lastIndexOf('-'));
        String buildNumber = previousLatestTimestamp.substring(previousLatestTimestamp.lastIndexOf('-') + 1,
                                                               previousLatestTimestamp.length());

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

    public void pathExists(String path,
                           String url,
                           boolean exists)
    {
        String url2 = getContextBaseUrl() + (url.startsWith("/") ? url : '/' + url);

        logger.debug("Path to artifact: " + url);

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
                .when()
                .get(url2)
                .peek()
                .then()
                .statusCode(exists ? 200 : 404);

    }

    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/metadata/" + storageId + "/" + repositoryId;

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", (basePath != null ? basePath : ""))
                .when()
                .post(url)
                .peek()
                .then()
                .statusCode(200);
    }

}
