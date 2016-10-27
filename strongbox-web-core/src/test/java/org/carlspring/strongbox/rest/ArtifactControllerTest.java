package org.carlspring.strongbox.rest;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.RestAssuredTest;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import com.jayway.restassured.response.ExtractableResponse;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.junit.Assert.*;

/**
 * Test cases for {@link ArtifactController}.
 *
 * @author Alex Oreshkevich, Martin Todorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestAssuredTest
public class ArtifactControllerTest
        extends RestAssuredBaseTest
{

    public static final String PACKAGING_JAR = "jar";

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                           "/local");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    private MetadataMerger metadataMerger;


    @BeforeClass
    public static void setUpClass()
            throws Exception
    {
        // to make this test idempotent (in general) we will check and remove previous generated artifacts if they are present
        // notice that we can't delete the whole repository basedir because it contains also .index, .trash etc.
        removeDir(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/org");
        removeDir(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/com");


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
    public void testPartialFetch()
            throws Exception
    {
        // test that given artifact exists
        String url = getContextBaseUrl() + "/storages/storage0/releases";
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";
        String artifactPath = url + pathToJar;

        assertPathExists(artifactPath);

        // read remote checksum
        String md5Remote = MessageDigestUtils.readChecksumFile(client.getArtifactAsStream(artifactPath + ".md5", true));
        String sha1Remote = MessageDigestUtils.readChecksumFile(
                client.getArtifactAsStream(artifactPath + ".sha1", true));

        logger.info("Remote md5 checksum " + md5Remote);
        logger.info("Remote sha1 checksum " + sha1Remote);

        // calculate local checksum for given algorithms
        InputStream is = client.getArtifactAsStream(artifactPath);
        logger.debug("Wrote " + is.available() + " bytes.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

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

        logger.debug("Read " + total + " bytes.");

        is = client.getArtifactAsStream(artifactPath, total);

        logger.debug("Skipped " + total + " bytes.");

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        logger.debug("Wrote " + total + " bytes.");
        logger.debug("Partial read, terminated after writing " + partialRead + " bytes.");
        logger.debug("Partial read, continued and wrote " + len2 + " bytes.");
        logger.debug("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        logger.debug("MD5   [Remote]: " + md5Remote);
        logger.debug("MD5   [Local ]: " + md5Local);

        logger.debug("SHA-1 [Remote]: " + sha1Remote);
        logger.debug("SHA-1 [Local ]: " + sha1Local);

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

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testCopyArtifactDirectory()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.2";

        // clean up directory from possible previous test executions
        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();
        if (artifactFileRestoredFromTrash.exists())
        {
            removeDir(artifactFileRestoredFromTrash);
        }

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

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

        client.delete("storage0", "releases", artifactPath);

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

        client.delete("storage0", "releases", artifactPath);

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

        String basePath = "storages/storage0/releases";

        ExtractableResponse repositoryRoot = client.getResourceWithResponse(basePath, "");
        ExtractableResponse trashDirectoryListing = client.getResourceWithResponse(basePath, ".trash");
        ExtractableResponse indexDirectoryListing = client.getResourceWithResponse(basePath, ".index");
        ExtractableResponse directoryListing = client.getResourceWithResponse(basePath,
                                                                       "org/carlspring/strongbox/browse");
        ExtractableResponse fileListing = client.getResourceWithResponse(basePath,
                                                                         "org/carlspring/strongbox/browse/foo-bar/1.0");
        ExtractableResponse invalidPath = client.getResourceWithResponse(basePath,
                                                                         "org/carlspring/strongbox/browse/1.0");

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

        logger.debug(directoryListingContent);

        assertTrue(directoryListingContent.contains("org/carlspring/strongbox/browse"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.jar"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.pom"));

        assertTrue(invalidPath.response().getStatusCode() == 404);
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

        String snapshotVersion1 = generator.createSnapshotVersion("3.1", 1);
        String snapshotVersion2 = generator.createSnapshotVersion("3.1", 2);
        String snapshotVersion3 = generator.createSnapshotVersion("3.1", 3);
        String snapshotVersion4 = generator.createSnapshotVersion("3.1", 4);

        Artifact artifact1WithTimestamp1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + snapshotVersion1);
        Artifact artifact1WithTimestamp2 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + snapshotVersion2);
        Artifact artifact1WithTimestamp3 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + snapshotVersion3);
        Artifact artifact1WithTimestamp4 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + snapshotVersion4);

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.setClient(client);

        String storageId = "storage0";
        String repositoryId = "snapshots";

        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp2, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp3, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp4, storageId, repositoryId);

        String path = ArtifactUtils.getVersionLevelMetadataPath(artifact1);
        String url = "/storages/" + storageId + "/" + repositoryId + "/";

        String metadataUrl = url + path;

        logger.info("[retrieveMetadata] Load metadata by URL " + metadataUrl);

        Metadata versionLevelMetadata = client.retrieveMetadata(url + path);

        assertNotNull(versionLevelMetadata);
        assertEquals("org.carlspring.strongbox.metadata", versionLevelMetadata.getGroupId());
        assertEquals("metadata-foo", versionLevelMetadata.getArtifactId());

        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion1, null, "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion1, "javadoc", "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion1, null, "pom");

        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion2, null, "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion2, "javadoc", "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion2, null, "pom");

        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion3, null, "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion3, "javadoc", "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion3, null, "pom");

        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion4, null, "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion4, "javadoc", "jar");
        checkSnapshotVersionExistsInMetadata(versionLevelMetadata, snapshotVersion4, null, "pom");

        assertNotNull(versionLevelMetadata.getVersioning().getLastUpdated());
    }

    private boolean checkSnapshotVersionExistsInMetadata(Metadata versionLevelMetadata,
                                                         String version,
                                                         String classifier,
                                                         String extension)
    {
        return versionLevelMetadata.getVersioning().getSnapshotVersions().stream()
                                   .filter(snapshotVersion ->
                                                   snapshotVersion.getVersion().equals(version) &&
                                                   snapshotVersion.getClassifier().equals(classifier) &&
                                                   snapshotVersion.getExtension().equals(extension)
                                   ).findAny().isPresent();
    }
}
