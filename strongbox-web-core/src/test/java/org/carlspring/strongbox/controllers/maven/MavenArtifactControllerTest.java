package org.carlspring.strongbox.controllers.maven;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationReportOperation;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Throwables;
import io.restassured.response.ExtractableResponse;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.artifact.PluginArtifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.maven.commons.util.ArtifactUtils.getArtifactFromGAVTC;
import static org.junit.Assert.*;

/**
 * Test cases for {@link MavenArtifactController}.
 *
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenArtifactControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final String REPOSITORY_RELEASES1 = "act-releases-1";

    private static final String REPOSITORY_RELEASES2 = "act-releases-2";

    private static final String REPOSITORY_SNAPSHOTS = "act-snapshots";

    private static File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

        MavenRepositoryConfiguration mavenRepositoryConfiguration = new MavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        Repository repository1 = new Repository(REPOSITORY_RELEASES1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository1.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repository1);

        // Generate releases
        // Used by testPartialFetch():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         new String[]{ "3.1",
                                       // Used by testPartialFetch()
                                       "3.2"
                                       // Used by testPartialFetch()
                         }
        );

        // Used by testCopy*():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.copy:copy-foo",
                         new String[]{ "1.1",
                                       // Used by testCopyArtifactFile()
                                       "1.2"
                                       // Used by testCopyArtifactDirectory()
                         }
        );

        // Used by testDelete():
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[]{ "1.2.1",
                                       // Used by testDeleteArtifactFile
                                       "1.2.2"
                                       // Used by testDeleteArtifactDirectory
                         }
        );
        generateMavenMetadata(STORAGE0, REPOSITORY_RELEASES1);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         new String[]{ "3.1",
                                       // Used by testPartialFetch()
                                       "3.2"
                                       // Used by testPartialFetch()
                         }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.strongbox.browse:foo-bar",
                         new String[]{ "1.0",
                                       // Used by testDirectoryListing()
                                       "2.4"
                                       // Used by testDirectoryListing()
                         }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, "releases").getAbsolutePath(),
                         "org.carlspring.strongbox.test:dynamic-privileges",
                         new String[]{ "1.0"
                                       // Used by testDynamicPrivilegeAssignmentForRepository()
                         }
        );

        Repository repository2 = new Repository(REPOSITORY_RELEASES2);
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setStorage(configurationManager.getConfiguration()
                                                   .getStorage(STORAGE0));
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repository2.setAllowsRedeployment(true);

        createRepository(repository2);

        Repository repository3 = new Repository(REPOSITORY_SNAPSHOTS);
        repository3.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
        repository3.setStorage(configurationManager.getConfiguration()
                                                   .getStorage(STORAGE0));

        createRepository(repository3);

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_RESOURCES).mkdirs();
    }

    @Override
    public void shutdown()
    {
        try
        {
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES1);
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES2);
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_SNAPSHOTS);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        super.shutdown();
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
        String artifactPath = "storages/storage-common-proxies/maven-central/" +
                              "org/carlspring/maven/derby-maven-plugin/1.9/derby-maven-plugin-1.9.jar";

        resolveArtifact(artifactPath, "1.9");
    }

    /**
     * Note: This test requires access to the Internet.
     *
     * @throws Exception
     */
    @Test
    public void testResolveViaProxyToMavenCentralInGroup()
            throws Exception
    {
        String artifactPath = "storages/storage-common-proxies/group-common-proxies/" +
                              "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar";

        resolveArtifact(artifactPath, "1.10");
    }

    private void resolveArtifact(String artifactPath,
                                 String version)
            throws NoSuchAlgorithmException, IOException
    {
        InputStream is = client.getResource(artifactPath);
        if (is == null)
        {
            fail("Failed to resolve 'derby-maven-plugin:" + version + ":jar' from Maven Central!");
        }

        FileOutputStream fos = new FileOutputStream(new File(TEST_RESOURCES, "derby-maven-plugin-" + version + ".jar"));
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
        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES1;
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
                         new String[]{ "1.1" }
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
    public void testNonExistingDirectoryDownload()
    {
        String path = "/storages/storage-common-proxies/maven-central/john/doe/";
        ExtractableResponse response = client.getResourceWithResponse(path,"");
        assertTrue("Wrong response", response.statusCode() == 404);
    }
    
    @Test
    public void testNonExistingArtifactInNonExistingDirectory()
    {
        String path = "/storages/storage-common-proxies/maven-central/john/doe/who.jar";
        ExtractableResponse response = client.getResourceWithResponse(path,"");
        assertTrue("Wrong response", response.statusCode() == 404);
    }
    
    @Test
    public void testNonExistingArtifactInExistingDirectory()
    {
        String path = "/storages/storage-common-proxies/maven-central/org/carlspring/maven/derby-maven-plugin/1.8/derby-maven-plugin-6.9.jar";
        ExtractableResponse response = client.getResourceWithResponse(path,"");
        assertTrue("Wrong response", response.statusCode() == 404);
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
                   trashDirectoryListing.response()
                                        .getStatusCode() == 404);

        assertFalse(".index directory should not be visible in directory listing!",
                    repositoryRootContent.contains(".index"));
        assertTrue(".index directory should not be browsable!",
                   indexDirectoryListing.response()
                                        .getStatusCode() == 404);

        logger.debug(directoryListingContent);

        assertTrue(directoryListingContent.contains("org/carlspring/strongbox/browse"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.jar"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.pom"));

        assertTrue(invalidPath.response()
                              .getStatusCode() == 404);
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

        assertNotNull(versionLevelMetadata.getVersioning()
                                          .getLastUpdated());
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
        Metadata groupLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" +
                                                              REPOSITORY_RELEASES2 + "/" +
                                                              ArtifactUtils.getGroupLevelMetadataPath(artifact1));

        assertNotNull(groupLevelMetadata);
        assertEquals(2, groupLevelMetadata.getPlugins().size());

        // Artifact Level metadata
        Metadata artifactLevelMetadata = client.retrieveMetadata("storages/" + STORAGE0 + "/" +
                                                                 REPOSITORY_RELEASES2 + "/" +
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
            logger.debug("Found " + results.getResults()
                                           .size() + " results in index of " +
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

        assertTrue("Failed to delete artifacts from Maven Index!!", results.getResults()
                                                                           .isEmpty());
        assertTrue(!metadata.getVersioning()
                            .getVersions()
                            .contains("1.2.2"));
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

        assertFalse(metadata.getVersioning()
                            .getVersions()
                            .contains("3.1-SNAPSHOT"));
    }

    private boolean checkSnapshotVersionExistsInMetadata(Metadata versionLevelMetadata,
                                                         String version,
                                                         String classifier,
                                                         String extension)
    {
        return versionLevelMetadata.getVersioning()
                                   .getSnapshotVersions()
                                   .stream()
                                   .anyMatch(snapshotVersion -> snapshotVersion.getVersion().equals(version) &&
                                                                snapshotVersion.getClassifier().equals(classifier) &&
                                                                snapshotVersion.getExtension().equals(extension)
                                   );
    }

    /**
     * User developer01 does not have general the ARTIFACTS_RESOLVE permission, but it's defined for single 'releases'
     * repository. So because of dynamic privileges assignment they will be able to get access to artifacts in that
     * repository.
     */
    @Test
    @WithUserDetails("developer01")
    public void testDynamicPrivilegeAssignmentForRepository()
    {
        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/releases";
        String pathToJar = "/org/carlspring/strongbox/test/dynamic-privileges/1.0/dynamic-privileges-1.0.jar";
        String artifactPath = url + pathToJar;

        int statusCode = given().header("user-agent", "Maven/*")
                                .contentType(MediaType.TEXT_PLAIN_VALUE)
                                .when()
                                .get(artifactPath)
                                .getStatusCode();

        assertEquals("Access was wrongly restricted for user with custom access model", 200, statusCode);
    }

    @Test
    public void shouldDownloadProxiedSnapshotArtifactFromGroup()
            throws Exception
    {
        ArtifactSnapshotVersion commonsHttpSnapshot = getCommonsHttpArtifactSnapshotVersionFromCarlspringRemote();

        if (commonsHttpSnapshot == null)
        {
            logger.debug("commonsHttpSnapshot was not found");
            return;
        }

        String url = getContextBaseUrl() +
                     "/storages/public/public-group/org/carlspring/commons/commons-http/" +
                     commonsHttpSnapshot.version + "/commons-http-" + commonsHttpSnapshot.timestampedVersion + ".jar";

        given().header("user-agent", "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDownloadProxiedSnapshotArtifactFromRemote()
            throws Exception
    {
        ArtifactSnapshotVersion commonsHttpSnapshot = getCommonsHttpArtifactSnapshotVersionFromCarlspringRemote();

        if (commonsHttpSnapshot == null)
        {
            logger.debug("commonsHttpSnapshot was not found");
            return;
        }

        String url = getContextBaseUrl() +
                     "/storages/storage-common-proxies/carlspring/org/carlspring/commons/commons-http/" +
                     commonsHttpSnapshot.version +
                     "/commons-http-" + commonsHttpSnapshot.timestampedVersion + ".jar";

        given().header("user-agent", "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void whatsup()
            throws Exception
    {
        String url = getContextBaseUrl() +
                     "/storages/storage0/snapshots/org/carlspring/maven/test-project/1.0.8-SNAPSHOT/test-project-1.0.8-20170823.221551-1.jar";

        given().header("user-agent", "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    private ArtifactSnapshotVersion getCommonsHttpArtifactSnapshotVersionFromCarlspringRemote()
            throws Exception
    {

        Metadata libraryMetadata = client.retrieveMetadata(
                "/storages/storage-common-proxies/carlspring/org/carlspring/commons/commons-http/maven-metadata.xml");

        if (libraryMetadata == null)
        {
            logger.debug("libraryMetadata not found");
            return null;
        }

        String commonsHttpSnapshotVersion = libraryMetadata.getVersioning().getVersions().stream().filter(
                v -> v.endsWith("SNAPSHOT")).findFirst().orElse(null);

        if (commonsHttpSnapshotVersion == null)
        {
            logger.debug("commonsHttpSnapshotVersion not found");
            return null;
        }

        Metadata artifactMetadata = client.retrieveMetadata(
                "/storages/storage-common-proxies/carlspring/org/carlspring/commons/commons-http/" +
                commonsHttpSnapshotVersion + "/maven-metadata.xml");

        if (artifactMetadata == null)
        {
            logger.debug("artifactMetadata not found");
            return null;
        }

        SnapshotVersion snapshotVersion = artifactMetadata.getVersioning().getSnapshotVersions().stream().findFirst().orElse(
                null);

        if (snapshotVersion == null)
        {
            logger.debug("snapshotVersion not found");
            return null;
        }

        return new ArtifactSnapshotVersion(commonsHttpSnapshotVersion, snapshotVersion.getVersion());
    }

    private static class ArtifactSnapshotVersion
    {

        private final String version;

        private final String timestampedVersion;


        private ArtifactSnapshotVersion(String version,
                                        String timestampedVersion)
        {
            this.version = version;
            this.timestampedVersion = timestampedVersion;
        }
    }
}
