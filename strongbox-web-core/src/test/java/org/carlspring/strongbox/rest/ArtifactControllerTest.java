package org.carlspring.strongbox.rest;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Headers;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.project.artifact.PluginArtifact;
import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.maven.commons.model.ModelWriter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

/**
 * Created by yury on 8/3/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class ArtifactControllerTest
        extends BackendBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
            "/local");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    public static final String PACKAGING_JAR = "jar";

    private MetadataMerger metadataMerger;

    private static final Logger logger = LoggerFactory.getLogger(ArtifactControllerTest.class);


    @Before
    public void setUpClass()
            throws Exception {
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.resolve.only:foo",
                         "1.1" // Used by testResolveViaProxy()
        );

        // Generate releases
        // Used by testPartialFetch():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         "3.1", // Used by testPartialFetch()
                         "3.2"  // Used by testPartialFetch()
        );

        // Used by testCopy*():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.copy:copy-foo",
                         "1.1", // Used by testCopyArtifactFile()
                         "1.2"  // Used by testCopyArtifactDirectory()
        );

        // Used by testDelete():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         "1.2.1", // Used by testDeleteArtifactFile
                         "1.2.2"  // Used by testDeleteArtifactDirectory
        );

        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         "3.1", // Used by testPartialFetch()
                         "3.2"  // Used by testPartialFetch()
        );

        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.browse:foo-bar",
                         "1.0", // Used by testDirectoryListing()
                         "2.4"  // Used by testDirectoryListing()
        );

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_RESOURCES).mkdirs();
    }


    @Test
    @WithUserDetails("admin")
    public void testUserAuth()
            throws Exception
    {

        String url = getContextBaseUrl() + "/storages/greet";


        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body(containsString("success"))
                .toString();
    }


    @Test
    @WithUserDetails("admin")
    public void testPartialFetch()
            throws Exception
    {
        // test that given artifact exists
        String url = getContextBaseUrl() + "/storages/storage0/releases";
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        logger.info("Getting " + url + "...");

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", pathToJar)
                .when()
                .get(url)
                //.peek()
                .then()
                .statusCode(200);

        // read remote checksum
        String md5Remote = MessageDigestUtils.readChecksumFile(
                getArtifactAsStream(pathToJar + ".md5", url)
        );
        String sha1Remote = MessageDigestUtils.readChecksumFile(
                getArtifactAsStream(pathToJar + ".sha1", url)
        );
        logger.info("Remote md5 checksum " + md5Remote);
        logger.info("Remote sha1 checksum " + sha1Remote);

        // calculate local checksum for given algorithms
        InputStream is = getArtifactAsStream(pathToJar, url);
        System.out.println("Wrote " + is.available() + " bytes.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /*ByteStreams.copy(is, baos); // full copying

        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);
        mdos.write(baos.toByteArray());

        int total = baos.size(); // FIXME
        mdos.flush();
        mdos.close();*/


        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);

        int size = 1024;
        byte[] bytes = new byte[size];
        int total = 0;
        int len;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            total += len;
            if (total >= size)
            {
                break;
            }
        }

        mdos.flush();

        bytes = new byte[size];
        is.close();

        System.out.println("Read " + total + " bytes.");

        is = getArtifactAsStream(pathToJar, url, total);

        System.out.println("Skipped " + total + " bytes.");

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        System.out.println("Wrote " + total + " bytes.");
        System.out.println("Partial read, terminated after writing " + partialRead + " bytes.");
        System.out.println("Partial read, continued and wrote " + len2 + " bytes.");
        System.out.println("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        System.out.println("MD5   [Remote]: " + md5Remote);
        System.out.println("MD5   [Local ]: " + md5Local);

        System.out.println("SHA-1 [Remote]: " + sha1Remote);
        System.out.println("SHA-1 [Local ]: " + sha1Local);

        File artifact = new File("target/partial-foo-3.1.jar");
        if (artifact.exists())
        {
            artifact.delete();
            artifact.createNewFile();
        }
        FileOutputStream output = new FileOutputStream(artifact);
        output.write(baos.toByteArray());
        output.close();

        assertEquals("Glued partial fetches did not match MD5 checksum!", md5Remote, md5Local);
        assertEquals("Glued partial fetches did not match SHA-1 checksum!", sha1Remote, sha1Local);
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

    public boolean pathExists(String path)
    {
        String url = getContextBaseUrl() + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path to artifact: " + url);

        //   WebTarget resource = getClientInstance().target(url);
        //  setupAuthentication(resource);

        //  Response response = resource.request(MediaType.TEXT_PLAIN).get();

        Integer response;

        response =
                given()
                        .contentType(ContentType.TEXT)
                        .when()
                        .get(url)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()
                        .body()
                        .path("entity");

        return response == 200;
    }

    @Test
    public void testCopyArtifactFile()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.1/copy-foo-1.1.jar";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();
        if (artifactFileRestoredFromTrash.exists())
        {
            artifactFileRestoredFromTrash.delete();
        }

        String url = getContextBaseUrl() + "/storages/copy";

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", artifactPath, "srcStorageId", "storage0", "srcRepositoryId", "releases",
                        "destStorageId", "storage0", "destRepositoryId", "releases-with-trash")
                .when()
                .post(url)
                .peek()
                .then()
                .statusCode(200)
                .extract();

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    @Ignore
    public void testCopyArtifactDirectory()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.2";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        String url = getContextBaseUrl() + "/storages/copy";

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", artifactPath, "srcStorageId", "storage0", "srcRepositoryId", "releases",
                        "destStorageId", "storage0", "destRepositoryId", "releases-with-trash")
                .when()
                .post(url)
                .peek()
                .then()
                .statusCode(200)
                .extract();

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testDeleteArtifactFile()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" +
                                        artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                   deletedArtifact.exists());

        String url = getContextBaseUrl() + "/storages/" + "storage0" + "/" + "releases";

        MockMvcResponse response = given()
                                           .contentType(MediaType.TEXT_PLAIN_VALUE)
                                           .params("path", artifactPath)
                                           .when()
                                           .delete(url)
                                           .peek()
                                           .then()
                                           .statusCode(200)
                                           .extract().response();

        String message = "Failed to delete artifact!";

        int status = response.getStatusCode();

        if (status == SC_UNAUTHORIZED || status == SC_FORBIDDEN)
        {
            // TODO Handle authentication exceptions in a right way
            throw new AuthenticationServiceException(message +
                                                     "\nUser is unauthorized to execute that operation. " +
                                                     "Check assigned roles and privileges.");
        }
        else if (status != 200)
        {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("\n ERROR ").append(status).append(" ").append(message).append("\n");
            Object entity = response.getBody();
            if (entity != null)
            {
                messageBuilder.append(entity.toString());
            }
            logger.error(messageBuilder.toString());
        }

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                    deletedArtifact.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.2";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" +
                                        artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                   deletedArtifact.exists());

        String url = getContextBaseUrl() + "/storages/" + "storage0" + "/" + "releases";

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", artifactPath)
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200)
                .extract();

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                    deletedArtifact.exists());
    }

    @Test
    public void testDirectoryListing()
            throws Exception
    {
        String artifactPath = "org/carlspring/strongbox/browse/foo-bar";

        File artifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" + artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + artifact.getAbsolutePath() + "'!", artifact.exists());

        ExtractableResponse repositoryRoot = getResourceWithResponse("");
        ExtractableResponse trashDirectoryListing = getResourceWithResponse(".trash");
        ExtractableResponse indexDirectoryListing = getResourceWithResponse(".index");
        ExtractableResponse directoryListing = getResourceWithResponse("/org/carlspring/strongbox/browse");
        ExtractableResponse fileListing = getResourceWithResponse("/org/carlspring/strongbox/browse/foo-bar/1.0");
        ExtractableResponse invalidPath = getResourceWithResponse("/org/carlspring/strongbox/browse/1.0");

        String repositoryRootContent = repositoryRoot.asString();
        String directoryListingContent = directoryListing.asString();
        String fileListingContent = fileListing.asString();

        assertFalse(".trash directory should not be visible in directory listing!",
                    repositoryRootContent.contains(".trash"));
        assertTrue(".trash directory should not be browsable!",
                   trashDirectoryListing.response().getStatusCode() == 404);

        assertFalse(".index directory should not be visible in directory listing!",
                    repositoryRootContent.contains(".index"));
        assertTrue(".index directory should not be browsable!",
                   indexDirectoryListing.response().getStatusCode() == 404);

        System.out.println(directoryListingContent);

        assertTrue(directoryListingContent.contains("org/carlspring/strongbox/browse"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.jar"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.pom"));

        assertTrue(invalidPath.response().getStatusCode() == 404);
    }

    private ExtractableResponse getResourceWithResponse(String pathVar)
    {

        String path = "/storages/storage0/releases";
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        ExtractableResponse repositoryRootContent = given()
                                                            .contentType(MediaType.TEXT_PLAIN_VALUE)
                                                            .param("path", pathVar)
                                                            .when()
                                                            .get(url)
                                                            .peek()
                                                            .then()
                                                            .extract();
        return repositoryRootContent;
    }

    @Test
    public void testMetadataAtVersionLevel()
            throws NoSuchAlgorithmException,
                   ArtifactOperationException,
                   IOException,
                   XmlPullParserException,
                   ArtifactTransportException
    {
        String ga = "org.carlspring.strongbox.metadata:metadata-foo";

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":3.1-SNAPSHOT");
        TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();
        Artifact artifact1WithTimestamp1 = ArtifactUtils.getArtifactFromGAVTC(
                ga + ":" + generator.createSnapshotVersion("3.1", 1));
        Artifact artifact1WithTimestamp2 = ArtifactUtils.getArtifactFromGAVTC(
                ga + ":" + generator.createSnapshotVersion("3.1", 2));
        Artifact artifact1WithTimestamp3 = ArtifactUtils.getArtifactFromGAVTC(
                ga + ":" + generator.createSnapshotVersion("3.1", 3));
        Artifact artifact1WithTimestamp4 = ArtifactUtils.getArtifactFromGAVTC(
                ga + ":" + generator.createSnapshotVersion("3.1", 4));

        // artifactDeployer.setClient(client);

        String storageId = "storage0";
        String repositoryId = "snapshots";

        generateAndDeployArtifact(artifact1WithTimestamp1, storageId, repositoryId);
        generateAndDeployArtifact(artifact1WithTimestamp2, storageId, repositoryId);
        generateAndDeployArtifact(artifact1WithTimestamp3, storageId, repositoryId);
        generateAndDeployArtifact(artifact1WithTimestamp4, storageId, repositoryId);

        String path = "storages/" + storageId + "/" + repositoryId + "/" +
                      ArtifactUtils.getVersionLevelMetadataPath(artifact1);

        String url = "storages/" + storageId + "/" + repositoryId;

        Metadata versionLevelMetadata = retrieveMetadata(path, url);

        Assert.assertNotNull(versionLevelMetadata);
        Assert.assertEquals("org.carlspring.strongbox.metadata", versionLevelMetadata.getGroupId());
        Assert.assertEquals("metadata-foo", versionLevelMetadata.getArtifactId());
        Assert.assertEquals(4, versionLevelMetadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(versionLevelMetadata.getVersioning().getLastUpdated());
        Assert.assertEquals(12, versionLevelMetadata.getVersioning().getSnapshotVersions().size());
    }

 /*   Metadata retrieveMetadata(String path, String url) throws IOException, XmlPullParserException {
        InputStream is = getArtifactAsStream(path, url);
        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        Metadata versionLevelMetadata = reader.read(is);
        return versionLevelMetadata;
    }*/

    /**
     * Looks up a storage by it's ID.
     */
    private void generateAndDeployArtifact(Artifact artifact,
                                           String storageId,
                                           String repositoryId,
                                           ArtifactDeployer artifactDeployer)
            throws FileNotFoundException, NoSuchAlgorithmException {
        File artifactFile = new File(artifactDeployer.getBasedir(), ArtifactUtils.convertArtifactToPath(artifact));
        System.out.println(artifactDeployer.getBasedir());
        System.out.println(artifact);
        System.out.println(artifactFile);
        ArtifactInputStream is = new ArtifactInputStream(artifact, new FileInputStream(artifactFile));
    }

    public void generateAndDeployArtifact(Artifact artifact,
                                          String storageId,
                                          String repositoryId)
            throws NoSuchAlgorithmException,
            XmlPullParserException,
            IOException,
            ArtifactOperationException {
        generateAndDeployArtifact(artifact, null, storageId, repositoryId, "jar");
    }

    public void generateAndDeployArtifact(Artifact artifact,
                                          String[] classifiers,
                                          String storageId,
                                          String repositoryId,
                                          String packaging)
            throws NoSuchAlgorithmException,
            XmlPullParserException,
            IOException,
            ArtifactOperationException {
        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.generatePom(artifact, packaging);
        artifactDeployer.createArchive(artifact);

        deploy(artifact, storageId, repositoryId);
        deployPOM(ArtifactUtils.getPOMArtifact(artifact), storageId, repositoryId);

        if (classifiers != null)
        {
            for (String classifier : classifiers)
            {
                // We're assuming the type of the classifier is the same as the one of the main artifact
                Artifact artifactWithClassifier = ArtifactUtils.getArtifactFromGAVTC(artifact.getGroupId() + ":" +
                        artifact.getArtifactId() + ":" +
                        artifact.getVersion() + ":" +
                        artifact.getType() + ":" +
                        classifier);
                artifactDeployer.generate(artifactWithClassifier);

                deploy(artifactWithClassifier, storageId, repositoryId);
            }
        }
        try
        {
            mergeMetada(artifact, storageId, repositoryId);
        }
        catch (ArtifactTransportException e)
        {
            // TODO SB-230: What should we do if we get ArtifactTransportException,
            // IOException or XmlPullParserException
            logger.error(e.getMessage(), e);
        }
    }

    public void generate(Artifact artifact)
            throws IOException,
            XmlPullParserException,
            NoSuchAlgorithmException {
        generatePom(artifact, PACKAGING_JAR);
        createArchive(artifact);
    }


    public void createArchive(Artifact artifact)
            throws NoSuchAlgorithmException,
            IOException {
        ZipOutputStream zos = null;

        File artifactFile = null;

        try {
            artifactFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), ArtifactUtils.convertArtifactToPath(artifact));

            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            artifactFile.getParentFile().mkdirs();

            zos = new ZipOutputStream(new FileOutputStream(artifactFile));

            createMavenPropertiesFile(artifact, zos);
            addMavenPomFile(artifact, zos);
            createRandomSizeFile(zos);
        } finally {
            ResourceCloser.close(zos, logger);

            generateChecksumsForArtifact(artifactFile);
        }
    }


    private void createMavenPropertiesFile(Artifact artifact, ZipOutputStream zos)
            throws IOException {
        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                artifact.getGroupId() + "/" +
                artifact.getArtifactId() + "/" +
                "pom.properties");
        zos.putNextEntry(ze);

        Properties properties = new Properties();
        properties.setProperty("groupId", artifact.getGroupId());
        properties.setProperty("artifactId", artifact.getArtifactId());
        properties.setProperty("version", artifact.getVersion());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, null);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        byte[] buffer = new byte[4096];
        int len;
        while ((len = bais.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        bais.close();
        zos.closeEntry();
    }


    private void addMavenPomFile(Artifact artifact, ZipOutputStream zos) throws IOException {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), ArtifactUtils.convertArtifactToPath(pomArtifact));

        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                artifact.getGroupId() + "/" +
                artifact.getArtifactId() + "/" +
                "pom.xml");
        zos.putNextEntry(ze);

        try (FileInputStream fis = new FileInputStream(pomFile)) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } finally {
            zos.closeEntry();
        }
    }

    private void createRandomSizeFile(ZipOutputStream zos)
            throws IOException {
        ZipEntry ze = new ZipEntry("random-size-file");
        zos.putNextEntry(ze);

        RandomInputStream ris = new RandomInputStream(true, 1000000);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = ris.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        ris.close();
        zos.closeEntry();
    }

    public void generatePom(Artifact artifact, String packaging)
            throws IOException,
            XmlPullParserException,
            NoSuchAlgorithmException {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), ArtifactUtils.convertArtifactToPath(pomArtifact));

        // Make sure the artifact's parent directory exists before writing the model.
        //noinspection ResultOfMethodCallIgnored
        pomFile.getParentFile().mkdirs();

        Model model = new Model();
        model.setGroupId(artifact.getGroupId());
        model.setArtifactId(artifact.getArtifactId());
        model.setVersion(artifact.getVersion());
        model.setPackaging(packaging);

        logger.debug("Generating pom file for " + artifact.toString() + "...");

        ModelWriter writer = new ModelWriter(model, pomFile);
        writer.write();

        generateChecksumsForArtifact(pomFile);
    }

    private void generateChecksumsForArtifact(File artifactFile)
            throws NoSuchAlgorithmException, IOException {
        InputStream is = new FileInputStream(artifactFile);
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is);

        int size = 4096;
        byte[] bytes = new byte[size];

        //noinspection StatementWithEmptyBody
        while (mdis.read(bytes, 0, size) != -1) ;

        mdis.close();

        String md5 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        String sha1 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        MessageDigestUtils.writeChecksum(artifactFile, EncryptionAlgorithmsEnum.MD5.getExtension(), md5);
        MessageDigestUtils.writeChecksum(artifactFile, EncryptionAlgorithmsEnum.SHA1.getExtension(), sha1);
    }


    private void deployPOM(Artifact artifact,
                           String storageId,
                           String repositoryId)
            throws NoSuchAlgorithmException,
            IOException,
            ArtifactOperationException {
        File pomFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), ArtifactUtils.convertArtifactToPath(artifact));

        InputStream is = new FileInputStream(pomFile);
        ArtifactInputStream ais = new ArtifactInputStream(artifact, is);

        addArtifact(artifact, storageId, repositoryId, ais);

        deployChecksum(ais, storageId, repositoryId, artifact);
    }


    public void deploy(Artifact artifact, String storageId, String repositoryId)
            throws ArtifactOperationException, IOException, NoSuchAlgorithmException, XmlPullParserException {
        File artifactFile = new File(ConfigurationResourceResolver.getVaultDirectory() +
                "/local", ArtifactUtils.convertArtifactToPath(artifact));
        ArtifactInputStream ais = new ArtifactInputStream(artifact, new FileInputStream(artifactFile));

        addArtifact(artifact, storageId, repositoryId, ais);

        //  deployChecksum(ais, storageId, repositoryId, artifact);
    }

    public void addArtifact(Artifact artifact,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException, IOException {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;
        String path = ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Deploying " + url + "...");

        String fileName = ArtifactUtils.getArtifactFileName(artifact);

        deployFile(is, url, fileName, path);
    }

    private void deployChecksum(ArtifactInputStream ais,
                                String storageId,
                                String repositoryId,
                                Artifact artifact)
            throws ArtifactOperationException, IOException
    {
        ais.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        ais.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        for (Map.Entry entry : ais.getHexDigests().entrySet())
        {
            final String algorithm = (String) entry.getKey();
            final String checksum = (String) entry.getValue();

            ByteArrayInputStream bais = new ByteArrayInputStream(checksum.getBytes());

            final String extensionForAlgorithm = EncryptionAlgorithmsEnum.fromAlgorithm(algorithm).getExtension();

            String artifactToPath = ArtifactUtils.convertArtifactToPath(artifact) + extensionForAlgorithm;
            String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;
            String artifactFileName = ais.getArtifactFileName() + extensionForAlgorithm;

            deployFile(bais, url, artifactFileName, artifactToPath);
        }
    }


    public void deployFile(InputStream is,
                           String url,
                           String fileName,
                           String path)
            throws ArtifactOperationException, IOException {

        String contentDisposition = "attachment; filename=\"" + fileName + "\"";
        byte[] bytes = ByteStreams.toByteArray(is);

        given().param("path", path)
                .header("Content-Disposition", contentDisposition)
                .body(new String(bytes))
                .when()
                .put(url)
                .peek()
                .then()
                .statusCode(200)
                .extract().response();

    }

    public void mergeMetada(Artifact artifact,
                            String storageId,
                            String repositoryId)
            throws ArtifactTransportException,
            IOException,
            XmlPullParserException,
            NoSuchAlgorithmException,
            ArtifactOperationException {
        if (metadataMerger == null)
        {
            metadataMerger = new MetadataMerger();
        }

        Metadata metadata;
        if (ArtifactUtils.isSnapshot(artifact.getVersion()))
        {
            String path = ArtifactUtils.getVersionLevelMetadataPath(artifact);
            metadata = metadataMerger.updateMetadataAtVersionLevel(artifact,
                    retrieveMetadata("storages/" + storageId + "/" + repositoryId,
                            ArtifactUtils.getVersionLevelMetadataPath(artifact)));

            createMetadataArchive(metadata, path);
            deployMetadata(metadata, path, storageId, repositoryId);
        }

        String path = ArtifactUtils.getArtifactLevelMetadataPath(artifact);
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, retrieveMetadata("storages/" + storageId + "/" +
                        repositoryId,
                ArtifactUtils.getArtifactLevelMetadataPath(artifact)));

        createMetadataArchive(metadata, path);
        deployMetadata(metadata, path, storageId, repositoryId);

        if (artifact instanceof PluginArtifact) {
            path = ArtifactUtils.getGroupLevelMetadataPath(artifact);
            metadata = metadataMerger.updateMetadataAtGroupLevel((PluginArtifact) artifact, retrieveMetadata("storages/" + storageId + "/" +
                            repositoryId,
                    ArtifactUtils.getGroupLevelMetadataPath(artifact)));
            createMetadataArchive(metadata, path);
            deployMetadata(metadata, path, storageId, repositoryId);
        }
    }

    private Metadata retrieveMetadata(String url, String path)
            throws ArtifactTransportException,
            IOException,
            XmlPullParserException {
        MockMvcResponse response = given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", path)
                .when()
                .get(getContextBaseUrl() + url)
                .then()
                .statusCode(200).extract().response();

        if (response.statusCode() == 200) {
            InputStream is = getArtifactAsStream(path, url);
            MetadataXpp3Reader reader = new MetadataXpp3Reader();

            return reader.read(is);
        }

        return null;
    }

    private void deployMetadata(Metadata metadata,
                                String metadataPath,
                                String storageId,
                                String repositoryId)
            throws IOException,
            NoSuchAlgorithmException,
            ArtifactOperationException
    {
        File metadataFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), metadataPath);

        InputStream is = new FileInputStream(metadataFile);
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is);

        addMetadata(metadata, metadataPath, storageId, repositoryId, is);
        deployChecksum(mdis,
                storageId,
                repositoryId,
                metadataPath.substring(0, metadataPath.lastIndexOf('/') + 1), "maven-metadata.xml");
    }

    public void addMetadata(Metadata metadata,
                            String path,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException, IOException, NoSuchAlgorithmException {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        logger.debug("Deploying " + url + "...");

        //deployMetadata(is, url, path.substring(path.lastIndexOf("/")));
        deployMetadata(metadata, path, storageId, repositoryId);
    }

    private void deployChecksum(MultipleDigestInputStream mdis,
                                String storageId,
                                String repositoryId,
                                String path,
                                String metadataFileName)
            throws ArtifactOperationException,
            IOException {
        mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        for (Map.Entry entry : mdis.getHexDigests().entrySet()) {
            final String algorithm = (String) entry.getKey();
            final String checksum = (String) entry.getValue();

            ByteArrayInputStream bais = new ByteArrayInputStream(checksum.getBytes());

            final String extensionForAlgorithm = EncryptionAlgorithmsEnum.fromAlgorithm(algorithm).getExtension();

            String artifactToPath = path + metadataFileName + extensionForAlgorithm;
            String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;
            String artifactFileName = metadataFileName + extensionForAlgorithm;

            deployFile(bais, url, artifactFileName, artifactToPath);
        }
    }

    protected void createMetadataArchive(Metadata metadata, String metadataPath)
            throws NoSuchAlgorithmException, IOException {
        OutputStream os = null;
        Writer writer = null;

        File metadataFile = null;

        try {
            metadataFile = new File(GENERATOR_BASEDIR.getAbsolutePath(), metadataPath);

            if (metadataFile.exists()) {
                metadataFile.delete();
            }

            // Make sure the artifact's parent directory exists before writing
            // the model.
            // noinspection ResultOfMethodCallIgnored
            metadataFile.getParentFile().mkdirs();

            os = new MultipleDigestOutputStream(metadataFile, new FileOutputStream(metadataFile));
            writer = WriterFactory.newXmlWriter(os);
            MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
            mappingWriter.write(writer, metadata);

            os.flush();
        } finally {
            ResourceCloser.close(os, logger);

            generateChecksumsForArtifact(metadataFile);
        }
    }

}
