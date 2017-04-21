package org.carlspring.strongbox.controllers.maven;

import static org.carlspring.maven.commons.util.ArtifactUtils.getArtifactFromGAVTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.artifact.PluginArtifact;
import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.response.ExtractableResponse;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link MavenArtifactController}.
 *
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenArtifactControllerTest
        extends RestAssuredBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

    private static final String REPOSITORY_RELEASES1 = "act-releases-1";

    private static final String REPOSITORY_RELEASES2 = "act-releases-2";

    private static final String REPOSITORY_SNAPSHOTS = "act-snapshots";

    private static boolean initialized = false;

    @Inject
    private ConfigurationManager configurationManager;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @PostConstruct
    public void initialize()
            throws Exception
    {
        if (initialized)
        {
            return;
        }
        initialized = true;
        GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

        Repository repository1 = new Repository(REPOSITORY_RELEASES1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository1.setIndexingEnabled(true);

        createRepository(repository1);

        // Generate releases
        // Used by testPartialFetch():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         new String[] { "3.1", // Used by testPartialFetch()
                                        "3.2"  // Used by testPartialFetch()
                         }
        );

        // Used by testCopy*():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.copy:copy-foo",
                         new String[] { "1.1", // Used by testCopyArtifactFile()
                                        "1.2"  // Used by testCopyArtifactDirectory()
                         }
        );

        // Used by testDelete():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[] { "1.2.1", // Used by testDeleteArtifactFile
                                        "1.2.2"  // Used by testDeleteArtifactDirectory
                         }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         new String[]{ "3.1", // Used by testPartialFetch()
                                       "3.2"  // Used by testPartialFetch()
                         }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.browse:foo-bar",
                         new String[]{ "1.0", // Used by testDirectoryListing()
                                       "2.4"  // Used by testDirectoryListing()
                         }
        );

        Repository repository2 = new Repository(REPOSITORY_RELEASES2);
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository2.setIndexingEnabled(true);
        repository2.setAllowsRedeployment(true);

        createRepository(repository2);

        Repository repository3 = new Repository(REPOSITORY_SNAPSHOTS);
        repository3.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
        repository3.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));

        createRepository(repository3);

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_RESOURCES).mkdirs();
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS));

        return repositories;
    }

    /**
     * Note: This test requires access to the Internet.
     *
     * @throws Exception
     */
    @Test
    public void testResolveViaProxyToMavenCentral()
            throws Exception
    {
        String artifactPath = "storages/public/public-group/org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar";

        InputStream is = client.getResource(artifactPath);
        if (is == null)
        {
            fail("Failed to resolve 'derby-maven-plugin:1.10:jar' from Maven Central!");
        }

        FileOutputStream fos = new FileOutputStream(new File(TEST_RESOURCES, "derby-maven-plugin-1.10.jar"));
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fos);

        int len;
        final int size = 1024;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);
        }

        mdos.flush();
        mdos.close();

        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".md5"));
        String sha1Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".sha1"));

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        logger.debug("MD5   [Remote]: " + md5Remote);
        logger.debug("MD5   [Local ]: " + md5Local);

        logger.debug("SHA-1 [Remote]: " + sha1Remote);
        logger.debug("SHA-1 [Local ]: " + sha1Local);

        assertEquals("MD5 checksums did not match!", md5Local, md5Remote);
        assertEquals("SHA-1 checksums did not match!", sha1Local, sha1Remote);
    }

    @Test
    public void testPartialFetch()
            throws Exception
    {
        // test that given artifact exists
        String url = getContextBaseUrl() + "/storages/" + STORAGE0 +"/" + REPOSITORY_RELEASES1;
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";
        String artifactPath = url + pathToJar;

        assertPathExists(artifactPath);

        // read remote checksum
        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".md5", true));
        String sha1Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".sha1", true));

        logger.info("Remote md5 checksum " + md5Remote);
        logger.info("Remote sha1 checksum " + sha1Remote);

        // calculate local checksum for given algorithms
        InputStream is = client.getResource(artifactPath);
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

        is = client.getResource(artifactPath, total);

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
            //noinspection ResultOfMethodCallIgnored
            artifact.delete();
            //noinspection ResultOfMethodCallIgnored
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
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.copy:copy-foo",
                         new String[] { "1.1" }
        );

        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2);

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.1/copy-foo-1.1.jar";

        File destArtifactFile = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();
        if (destArtifactFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            destArtifactFile.delete();
        }

        client.copy(artifactPath,
                    STORAGE0,
                    REPOSITORY_RELEASES1,
                    STORAGE0,
                    REPOSITORY_RELEASES2);

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   destArtifactFile.exists());
    }

    @Test
    public void testCopyArtifactDirectory()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/" + REPOSITORY_RELEASES2);

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
                    STORAGE0,
                    REPOSITORY_RELEASES1,
                    STORAGE0,
                    REPOSITORY_RELEASES2);

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testDeleteArtifactFile()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";

        File deletedArtifact = new File(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                                        artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                   deletedArtifact.exists());

        client.delete(STORAGE0, REPOSITORY_RELEASES1, artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                    deletedArtifact.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.2";

        File deletedArtifact = new File(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                                        artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                   deletedArtifact.exists());

        client.delete(STORAGE0, REPOSITORY_RELEASES1, artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                    deletedArtifact.exists());
    }

    @Test
    public void testDirectoryListing()
            throws Exception
    {
        String artifactPath = "org/carlspring/strongbox/browse/foo-bar";

        File artifact = new File(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(), artifactPath)
                                .getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + artifact.getAbsolutePath() + "'!", artifact.exists());

        String basePath = "storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES1;

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

        Artifact artifact1 = getArtifactFromGAVTC(ga + ":3.1-SNAPSHOT");

        String snapshotVersion1 = createSnapshotVersion("3.1", 1);
        String snapshotVersion2 = createSnapshotVersion("3.1", 2);
        String snapshotVersion3 = createSnapshotVersion("3.1", 3);
        String snapshotVersion4 = createSnapshotVersion("3.1", 4);

        Artifact artifact1WithTimestamp1 = getArtifactFromGAVTC(ga + ":" + snapshotVersion1);
        Artifact artifact1WithTimestamp2 = getArtifactFromGAVTC(ga + ":" + snapshotVersion2);
        Artifact artifact1WithTimestamp3 = getArtifactFromGAVTC(ga + ":" + snapshotVersion3);
        Artifact artifact1WithTimestamp4 = getArtifactFromGAVTC(ga + ":" + snapshotVersion4);

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);

        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp2, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp3, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp4, STORAGE0, REPOSITORY_SNAPSHOTS);

        String path = ArtifactUtils.getVersionLevelMetadataPath(artifact1);
        String url = "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS + "/";

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

    @Test
    public void testMetadataAtGroupAndArtifactIdLevel()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        // Given
        // Plugin Artifacts
        String groupId = "org.carlspring.strongbox.metadata";
        String artifactId1 = "metadata-foo-maven-plugin";
        String artifactId2 = "metadata-faa-maven-plugin";
        String artifactId3 = "metadata-foo";
        String version1 = "3.1";
        String version2 = "3.2";

        Artifact artifact1 = getArtifactFromGAVTC(groupId + ":" + artifactId1 + ":" + version1);
        Artifact artifact2 = getArtifactFromGAVTC(groupId + ":" + artifactId2 + ":" + version1);
        Artifact artifact3 = getArtifactFromGAVTC(groupId + ":" + artifactId1 + ":" + version2);
        Artifact artifact4 = getArtifactFromGAVTC(groupId + ":" + artifactId2 + ":" + version2);

        // Artifacts
        Artifact artifact5 = getArtifactFromGAVTC(groupId + ":" + artifactId3 + ":" + version1);
        Artifact artifact6 = getArtifactFromGAVTC(groupId + ":" + artifactId3 + ":" + version2);

        Plugin p1 = new Plugin();
        p1.setGroupId(artifact1.getGroupId());
        p1.setArtifactId(artifact1.getArtifactId());
        p1.setVersion(artifact1.getVersion());

        Plugin p2 = new Plugin();
        p2.setGroupId(artifact2.getGroupId());
        p2.setArtifactId(artifact2.getArtifactId());
        p2.setVersion(artifact2.getVersion());

        Plugin p3 = new Plugin();
        p3.setGroupId(artifact3.getGroupId());
        p3.setArtifactId(artifact3.getArtifactId());
        p3.setVersion(artifact3.getVersion());

        Plugin p4 = new Plugin();
        p4.setGroupId(artifact4.getGroupId());
        p4.setArtifactId(artifact4.getArtifactId());
        p4.setVersion(artifact4.getVersion());

        PluginArtifact a = new PluginArtifact(p1, artifact1);
        PluginArtifact b = new PluginArtifact(p2, artifact2);
        PluginArtifact c = new PluginArtifact(p3, artifact3);
        PluginArtifact d = new PluginArtifact(p4, artifact4);

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);

        // When
        artifactDeployer.generateAndDeployArtifact(a, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(b, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(c, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(d, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(artifact5, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(artifact6, STORAGE0, REPOSITORY_RELEASES2);

        // Then
        // Group level metadata
        Metadata groupLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2 + "/" +
                                                              ArtifactUtils.getGroupLevelMetadataPath(artifact1));

        assertNotNull(groupLevelMetadata);
        assertEquals(2, groupLevelMetadata.getPlugins().size());

        // Artifact Level metadata
        Metadata artifactLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2 + "/" +
                                                                 ArtifactUtils.getArtifactLevelMetadataPath(artifact1));

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId1, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        artifactLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2 + "/" +
                                                        ArtifactUtils.getArtifactLevelMetadataPath(artifact2));

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId2, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        artifactLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2 + "/" +
                                                        ArtifactUtils.getArtifactLevelMetadataPath(artifact5));

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId3, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());
    }

    @Test
    public void testUpdateMetadataOnDeleteReleaseVersionDirectory()
            throws Exception
    {
        // https://youtrack.carlspring.org/issue/SB-757#comment=84-648
        Assume.assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        // Given
        String groupId = "org.carlspring.strongbox.delete-metadata";
        String artifactId = "metadata-foo";
        String version1 = "1.2.1";
        String version2 = "1.2.2";

        Artifact artifact1 = getArtifactFromGAVTC(groupId + ":" + artifactId + ":" + version1);
        Artifact artifact2 = getArtifactFromGAVTC(groupId + ":" + artifactId + ":" + version2);
        Artifact artifact3 = getArtifactFromGAVTC(groupId + ":" + artifactId + ":" + version2 + ":jar:javadoc");

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);

        artifactDeployer.generateAndDeployArtifact(artifact1, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(artifact2, STORAGE0, REPOSITORY_RELEASES2);
        artifactDeployer.generateAndDeployArtifact(artifact3, STORAGE0, REPOSITORY_RELEASES2);

        // Run a search against the index and get a list of all the artifacts matching this exact GAV
        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES2,
                                                  "+g:" + groupId + " " +
                                                  "+a:" + artifactId + " " +
                                                  "+v:" + "1.2.2",
                                                  MavenIndexerSearchProvider.ALIAS);

        SearchResults results = artifactSearchService.search(request);

        if (!results.getResults().isEmpty())
        {
            logger.debug("Found " + results.getResults().size() + " results in index of " +
                         STORAGE0 + ":" + REPOSITORY_RELEASES2 + IndexTypeEnum.LOCAL.getType() + ".");
        }

        for (SearchResult result : results.getResults())
        {
            String artifactPath = result.getArtifactCoordinates().toPath();

            logger.debug(result.getArtifactCoordinates() + "(" + artifactPath + ")");
        }

        assertEquals("Incorrect number of results yielded from search against Maven Index!",
                     2,
                     results.getResults().size());

        // When
        String path = "org/carlspring/strongbox/delete-metadata/metadata-foo/1.2.2";
        client.delete(STORAGE0, REPOSITORY_RELEASES2, path);

        // Then
        Metadata metadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES2 + "/" +
                                                    ArtifactUtils.getArtifactLevelMetadataPath(artifact1));

        // Re-run the search and check, if the results are now different
        results = artifactSearchService.search(request);

        assertTrue("Failed to delete artifacts from Maven Index!!", results.getResults().isEmpty());
        assertTrue(!metadata.getVersioning().getVersions().contains("1.2.2"));
    }

    @Test
    public void testUpdateMetadataOnDeleteSnapshotVersionDirectory()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        // Given
        String ga = "org.carlspring.strongbox.metadata:metadata-foo";

        Artifact artifact1 = getArtifactFromGAVTC(ga + ":3.1-SNAPSHOT");
        Artifact artifact1WithTimestamp1 = getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 1));
        Artifact artifact1WithTimestamp2 = getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 2));
        Artifact artifact1WithTimestamp3 = getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 3));
        Artifact artifact1WithTimestamp4 = getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 4));

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);

        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp2, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp3, STORAGE0, REPOSITORY_SNAPSHOTS);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp4, STORAGE0, REPOSITORY_SNAPSHOTS);

        String path = "org/carlspring/strongbox/metadata/metadata-foo/3.1-SNAPSHOT";

        // When
        client.delete(STORAGE0, REPOSITORY_SNAPSHOTS, path);

        // Then
        Metadata metadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS + "/" +
                                                    ArtifactUtils.getArtifactLevelMetadataPath(artifact1));

        assertFalse(metadata.getVersioning().getVersions().contains("3.1-SNAPSHOT"));
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

    /**
     * User developer01 do not have general ARTIFACTS_RESOLVE permission, but it's defined for single 'act-releases-1'
     * repository. So because of dynamic privileges assignment he will be able to get access to artifacts in that
     * repository.
     */
    @Test
    @WithUserDetails("developer01")
    public void testDynamicPrivilegeAssignmentForRepository()
    {
        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES1;
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";
        String artifactPath = url + pathToJar;

        int statusCode = given().header("user-agent", "Maven/*")
                                .contentType(MediaType.TEXT_PLAIN_VALUE)
                                .when()
                                .get(artifactPath)
                                .getStatusCode();

        assertEquals(200, statusCode);
    }

}
