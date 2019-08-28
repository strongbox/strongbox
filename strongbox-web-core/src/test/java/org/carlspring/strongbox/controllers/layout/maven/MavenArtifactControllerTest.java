package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.artifact.PluginArtifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils.getArtifactLevelMetadataPath;
import static org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils.getGroupLevelMetadataPath;
import static org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils.getVersionLevelMetadataPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

/**
 * Test cases for {@link MavenArtifactController}.
 *
 * @author Alex Oreshkevich
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class MavenArtifactControllerTest
        extends MavenRestAssuredBaseTest
{
    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactControllerTest.class);

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final String REPOSITORY_RELEASES = "releases";

    private static final String REPOSITORY_RELEASES1 = "mact-releases-1";

    private static final String REPOSITORY_RELEASES2 = "mact-releases-2";

    private static final String REPOSITORY_SNAPSHOTS = "mact-snapshots";

    private static final String REPOSITORY_RELEASES_OUT_OF_SERVICE = "mact-releases-out-of-service";

    private static Path pluginXmlFilePath;

    @Spy
    private Artifact artifact1 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-foo-maven-plugin" + ":" +
                                                                             "3.1");
    @Spy
    private Artifact artifact2 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-faa-maven-plugin" + ":" +
                                                                             "3.1");
    @Spy
    private Artifact artifact3 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-foo-maven-plugin" + ":" +
                                                                             "3.2");
    @Spy
    private Artifact artifact4 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-faa-maven-plugin" + ":" +
                                                                             "3.2");
    @Spy
    private Artifact artifact5 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-foo" + ":" +
                                                                             "3.1");
    @Spy
    private Artifact artifact6 = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.strongbox.metadata" + ":" +
                                                                             "metadata-foo" + ":" +
                                                                             "3.2");
    @Inject
    private ArtifactEntryService artifactEntryService;

    private MavenArtifactDeployer defaultMavenArtifactDeployer;

    @Inject
    private PropertiesBooter propertiesBooter;

    @BeforeAll
    static void setUpBeforeAll()
            throws IOException
    {
        Files.createDirectories(Paths.get(TEST_RESOURCES));
    }

    @AfterAll
    static void down()
    {
        deleteTestResources();
    }

    private static void deleteTestResources()
    {
        if (pluginXmlFilePath == null)
        {
            return;
        }
        Path dirPath = pluginXmlFilePath.getParent().getParent().getParent();
        try
        {
            Files.walk(dirPath)
                 .map(Path::toFile)
                 .sorted(Comparator.comparing(File::isDirectory))
                 .forEach(File::delete);
        }
        catch (IOException e)
        {
            logger.error("Error while deleting the test resources", e);
        }
    }

    private static void writeToZipFile(Path path,
                                       ZipOutputStream zipStream)
            throws Exception
    {
        InputStream fis = Files.newInputStream(path);
        String zipEntryName = FilenameUtils.separatorsToUnix(path.toString());
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
        {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();

    }

    private static File createJarFile(String artifactId)
            throws Exception
    {
        Path parentPluginPath = pluginXmlFilePath.getParent();
        Path artifactPluginPath = parentPluginPath.resolve(artifactId + ".jar");
        try (OutputStream fos = Files.newOutputStream(artifactPluginPath);
             ZipOutputStream zipOS = new ZipOutputStream(fos))
        {
            writeToZipFile(pluginXmlFilePath.resolve("plugin.xml"), zipOS);
            System.out.println();

        }
        catch (IOException e)
        {
            logger.error("Error while creating the JAR file", e);
        }

        return artifactPluginPath.toFile();
    }

    private static void createPluginXmlFile(String groupId,
                                            String artifactId,
                                            String version)
            throws Exception
    {
        Path filePath = Paths.get("").toAbsolutePath().normalize();
        pluginXmlFilePath = filePath.resolve("src/test/resources/temp/" + artifactId + "/META-INF/maven");
        Files.createDirectories(pluginXmlFilePath);

        String xmlSource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<plugin>\n" +
                           "  <name>Apache Maven Dependency Plugin</name>\n" +
                           "  <description>Provides utility goals to work with dependencies like copying, unpacking, analyzing, resolving and many more.</description>\n" +
                           "  <groupId>" + groupId + "</groupId>\n" +
                           "  <artifactId>" + artifactId + "</artifactId>\n" +
                           "  <version>" + version + "</version>\n" +
                           "  <goalPrefix>dependency</goalPrefix>\n" +
                           "</plugin>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

        // Write the parsed document to an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(pluginXmlFilePath.resolve("plugin.xml").toFile());
        transformer.transform(source, result);
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        MockitoAnnotations.initMocks(this);
        defaultMavenArtifactDeployer = buildArtifactDeployer(Paths.get(""));
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

        OutputStream fos = Files.newOutputStream(Paths.get(TEST_RESOURCES, "derby-maven-plugin-" + version + ".jar"));
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

        String md5Remote = MessageDigestUtils.readChecksumFile(
                client.getResource(artifactPath + EncryptionAlgorithmsEnum.MD5.getExtension()));

        String sha1Remote = MessageDigestUtils.readChecksumFile(
                client.getResource(artifactPath + EncryptionAlgorithmsEnum.SHA1.getExtension()));

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        logger.debug("MD5   [Remote]: {}", md5Remote);
        logger.debug("MD5   [Local ]: {}", md5Local);

        logger.debug("SHA-1 [Remote]: {}", sha1Remote);
        logger.debug("SHA-1 [Local ]: {}", sha1Local);

        assertEquals(md5Local, md5Remote, "MD5 checksums did not match!");
        assertEquals(sha1Local, sha1Remote, "SHA-1 checksums did not match!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testHeadersFetch(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                  setup = MavenIndexedRepositorySetup.class)
                                 Repository repository,
                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                    id = "org.carlspring.strongbox.browse:foo-bar",
                                                    versions = "2.4")
                                 Path artifactPath)
    {
        /* Hosted Repository */
        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        Path artifactJarPath = repositoryPath.relativize(artifactPath);
        Path artifactPomPath = artifactJarPath.resolveSibling(
                artifactJarPath.getFileName().toString().replace(".jar", ".pom"));

        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES1 + "/" +
                     artifactPomPath.toString();

        Headers headersFromGET = client.getHeadersFromGET(url);
        Headers headersFromHEAD = client.getHeadersfromHEAD(url);
        assertHeadersEquals(headersFromGET, headersFromHEAD);
    }

    private void assertHeadersEquals(Headers h1,
                                     Headers h2)
    {
        assertNotNull(h1);
        assertNotNull(h2);

        for (Header header : h1)
        {
            if (h2.hasHeaderWithName(header.getName()))
            {
                assertEquals(header.getValue(), h2.getValue(header.getName()));
            }
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testPartialFetch(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                  setup = MavenIndexedRepositorySetup.class)
                                 Repository repository,
                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                    id = "org.carlspring.strongbox.partial:partial-foo",
                                                    versions = "3.1")
                                 Path artifactPath)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        // test that given artifact exists
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/";
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        String pathToJar = RepositoryFiles.relativizePath(artifactRepositoryPath);
        String artifactPathStr = url + pathToJar;

        assertPathExists(artifactPathStr);

        // read remote checksum
        String md5Remote = MessageDigestUtils.readChecksumFile(
                client.getResource(artifactPathStr + EncryptionAlgorithmsEnum.MD5.getExtension(), true));

        String sha1Remote = MessageDigestUtils.readChecksumFile(
                client.getResource(artifactPathStr + EncryptionAlgorithmsEnum.SHA1.getExtension(), true));

        logger.debug("Remote md5 checksum {}", md5Remote);
        logger.debug("Remote sha1 checksum {}", sha1Remote);

        // calculate local checksum for given algorithms
        InputStream is = client.getResource(artifactPathStr);
        logger.debug("Wrote {} bytes.", is.available());

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

        logger.debug("Read {} bytes,", total);

        is = client.getResource(artifactPathStr, total);

        logger.debug("Skipped {} bytes.", total);

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        logger.debug("Wrote {} bytes.", total);
        logger.debug("Partial read, terminated after writing {} bytes.", partialRead);
        logger.debug("Partial read, continued and wrote {} bytes.", len2);
        logger.debug("Partial reads: total written bytes: {}.", partialRead + len2);

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        logger.debug("MD5   [Remote]: {}", md5Remote);
        logger.debug("MD5   [Local ]: {}", md5Local);

        logger.debug("SHA-1 [Remote]: {}", sha1Remote);
        logger.debug("SHA-1 [Local ]: {}", sha1Local);

        Path artifact = Paths.get("target/partial-foo-3.1.jar");
        Files.deleteIfExists(artifact);
        Files.createFile(artifact);

        OutputStream output = Files.newOutputStream(artifact);
        output.write(baos.toByteArray());
        output.close();

        assertEquals(md5Remote, md5Local, "Glued partial fetches did not match MD5 checksum!");
        assertEquals(sha1Remote, sha1Local, "Glued partial fetches did not match SHA-1 checksum!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testCopyArtifactFile(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository1,
                                     @MavenRepository(repositoryId = REPOSITORY_RELEASES2,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository2,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                        id = "org.carlspring.strongbox.copy:copy-foo",
                                                        versions = "1.1")
                                     Path artifactPath)
            throws IOException
    {
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);

        client.copy(artifactRepositoryPathStr,
                    repository1.getStorage().getId(),
                    repository1.getId(),
                    repository2.getStorage().getId(),
                    repository2.getId());

        RootRepositoryPath repositoryPath2 = repositoryPathResolver.resolve(repository2);
        Path destArtifactPath = repositoryPath2.resolve(artifactRepositoryPathStr);
        assertTrue(Files.exists(destArtifactPath),
                   "Failed to copy artifact to destination repository '" + destArtifactPath + "'!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testCopyArtifactDirectory(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                           setup = MavenIndexedRepositorySetup.class)
                                          Repository repository1,
                                          @MavenRepository(repositoryId = REPOSITORY_RELEASES2,
                                                           setup = MavenIndexedRepositorySetup.class)
                                          Repository repository2,
                                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                             id = "org.carlspring.strongbox.copy:copy-foo",
                                                             versions = "1.2")
                                          Path artifactPath)
            throws IOException
    {
        RepositoryPath artifactDirectoryPath = (RepositoryPath) artifactPath.getParent().normalize();
        String artifactDirectoryPathStr = RepositoryFiles.relativizePath(artifactDirectoryPath);

        client.copy(artifactDirectoryPathStr,
                    repository1.getStorage().getId(),
                    repository1.getId(),
                    repository2.getStorage().getId(),
                    repository2.getId());

        RootRepositoryPath repositoryPath2 = repositoryPathResolver.resolve(repository2);
        Path destArtifactPath = repositoryPath2.resolve(artifactDirectoryPathStr);
        assertTrue(Files.exists(destArtifactPath),
                   "Failed to copy artifact to destination repository '" + destArtifactPath + "'!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeleteArtifactFile(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                        setup = MavenIndexedRepositorySetup.class)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                          id = "com.artifacts.to.delete.releases:delete-foo",
                                                          versions = "1.2.1")
                                       Path artifactPath)
            throws Exception
    {
        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        final String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);

        assertTrue(Files.exists(artifactRepositoryPath),
                   "Failed to locate artifact file '" + artifactRepositoryPath + "'!");

        client.delete(repository.getStorage().getId(),
                      repository.getId(),
                      artifactRepositoryPathStr);

        assertTrue(Files.notExists(artifactRepositoryPath),
                   "Failed to delete artifact file '" + artifactRepositoryPath + "'!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeleteArtifactDirectory(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                             setup = MavenIndexedRepositorySetup.class)
                                            Repository repository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                               id = "com.artifacts.to.delete.releases:delete-foo",
                                                               versions = "1.2.2")
                                            Path artifactPath)
            throws Exception
    {
        RepositoryPath artifactDirectoryPath = (RepositoryPath) artifactPath.getParent().normalize();
        String artifactDirectoryPathStr = RepositoryFiles.relativizePath(artifactDirectoryPath);

        assertTrue(Files.exists(artifactDirectoryPath),
                   "Failed to locate artifact directory '" + artifactDirectoryPath + "'!");

        client.delete(repository.getStorage().getId(),
                      repository.getId(),
                      artifactDirectoryPathStr);

        assertTrue(Files.notExists(artifactDirectoryPath),
                   "Failed to delete artifact file '" + artifactDirectoryPath + "'!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void whenRepositoryIsOutOfServiceWeShouldDisallowArtifactDeployment(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_OUT_OF_SERVICE)
            @RepositoryAttributes(status = RepositoryStatusEnum.OUT_OF_SERVICE)
            Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_OUT_OF_SERVICE,
                               id = "com.artifacts.to.delete.releases:delete-foo",
                               versions = "1.2.2")
            Path artifactPath)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        final String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);
        final String unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifactRepositoryPathStr);

        String url = String.format("/storages/%s/%s/%s", storageId, repositoryId, unixBasedRelativePath);

        MockMvcResponse mockMvcResponse = client.put2(url,
                                                      "<body/>",
                                                      MediaType.APPLICATION_XML_VALUE);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), mockMvcResponse.statusCode());
    }

    @Test
    public void testDirectoryDownload()
    {
        String path = "/storages/storage-common-proxies/maven-central/john/doe/";
        ExtractableResponse response = client.getResourceWithResponse(path, "");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(),
                     "The specified path should not ends with `/` character!");
    }

    @Test
    public void testNonExistingArtifactInExistingDirectory()
    {
        String path = "/storages/storage-common-proxies/maven-central/org/carlspring/maven/derby-maven-plugin/1.8/derby-maven-plugin-6.9.jar";
        ExtractableResponse response = client.getResourceWithResponse(path, "");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Wrong response");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDirectoryListing(@MavenRepository(repositoryId = REPOSITORY_RELEASES1,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                        id = "org.carlspring.strongbox.browse:foo-bar",
                                                        versions = "1.0")
                                     Path artifactPath)
    {
        assertTrue(Files.exists(artifactPath), "Failed to locate artifact file '" + artifactPath + "'!");

        String basePath = "/api/browse/" + repository.getStorage().getId() + "/" + repository.getId();

        ExtractableResponse repositoryRoot = client.getResourceWithResponse(basePath, "");
        ExtractableResponse trashDirectoryListing = client.getResourceWithResponse(basePath, ".trash");
        ExtractableResponse directoryListing = client.getResourceWithResponse(basePath,
                                                                              "org/carlspring/strongbox/browse/");
        ExtractableResponse fileListing = client.getResourceWithResponse(basePath,
                                                                         "org/carlspring/strongbox/browse/foo-bar/1.0/");
        ExtractableResponse invalidPath = client.getResourceWithResponse(basePath,
                                                                         "org/carlspring/strongbox/browse/1.0/");

        String repositoryRootContent = repositoryRoot.asString();
        String directoryListingContent = directoryListing.asString();
        String fileListingContent = fileListing.asString();

        assertFalse(repositoryRootContent.contains(".trash"),
                    ".trash directory should not be visible in directory listing!");
        assertEquals(HttpStatus.NOT_FOUND.value(), trashDirectoryListing.response().getStatusCode(),
                     ".trash directory should not be browsable!");

        logger.debug(directoryListingContent);

        assertTrue(directoryListingContent.contains("org/carlspring/strongbox/browse"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.jar"));
        assertTrue(fileListingContent.contains("foo-bar-1.0.pom"));

        assertEquals(HttpStatus.NOT_FOUND.value(), invalidPath.response().getStatusCode());

        assertFalse(repositoryRootContent.contains(LayoutFileSystem.INDEX),
                    ".index directory should not be visible in directory listing!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testMetadataAtVersionLevel(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS,
                                                            policy = RepositoryPolicyEnum.SNAPSHOT)
                                           Repository repository,
                                           @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                              id = "org.carlspring.strongbox.metadata:metadata-foo",
                                                              versions = { "3.1-20190812.124500-1",
                                                                           "3.1-20190812.124600-2",
                                                                           "3.1-20190812.124700-3",
                                                                           "3.1-20190812.124800-4" })
                                           List<Path> artifactsPaths)
            throws IOException,
                   XmlPullParserException,
                   ArtifactTransportException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        mavenMetadataServiceHelper.generateMavenMetadata(repository);

        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactsPaths.get(0).normalize();
        String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);
        String unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifactRepositoryPathStr);
        Artifact snapshotArtifact = MavenArtifactUtils.convertPathToArtifact(unixBasedRelativePath);

        assertNotNull(snapshotArtifact);
        String metadataPath = String.format("storages/%s/%s/%s",
                                            storageId,
                                            repositoryId,
                                            getVersionLevelMetadataPath(snapshotArtifact));

        logger.debug("[retrieveMetadata] Load metadata by URL {}", metadataPath);

        Metadata versionLevelMetadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(versionLevelMetadata);
        assertEquals("org.carlspring.strongbox.metadata", versionLevelMetadata.getGroupId());
        assertEquals("metadata-foo", versionLevelMetadata.getArtifactId());

        for (Path artifactPath : artifactsPaths)
        {
            artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
            artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);
            unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifactRepositoryPathStr);
            snapshotArtifact = MavenArtifactUtils.convertPathToArtifact(unixBasedRelativePath);
            assertNotNull(snapshotArtifact);

            checkSnapshotVersionExistsInMetadata(versionLevelMetadata,
                                                 snapshotArtifact.getVersion(),
                                                 null,
                                                 "jar");

            checkSnapshotVersionExistsInMetadata(versionLevelMetadata,
                                                 snapshotArtifact.getVersion(),
                                                 "javadoc",
                                                 "jar");

            checkSnapshotVersionExistsInMetadata(versionLevelMetadata,
                                                 snapshotArtifact.getVersion(),
                                                 null,
                                                 "pom");
        }

        assertNotNull(versionLevelMetadata.getVersioning().getLastUpdated());
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testMetadataAtGroupAndArtifactIdLevel(@MavenRepository(repositoryId = REPOSITORY_RELEASES2,
                                                                       setup = MavenIndexedRepositorySetup.class)
                                                      Repository repository)
            throws Exception
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        // Plugin Artifacts
        String groupId = "org.carlspring.strongbox.metadata";
        String artifactId1 = "metadata-foo-maven-plugin";
        String artifactId2 = "metadata-faa-maven-plugin";
        String artifactId3 = "metadata-foo";
        String version1 = "3.1";
        String version2 = "3.2";

        createPluginXmlFile(groupId, artifactId1, version1);
        File file = createJarFile(artifactId1 + "-" + version1);
        doReturn(file).when(artifact1).getFile();

        createPluginXmlFile(groupId, artifactId2, version1);
        file = createJarFile(artifactId2 + "-" + version1);
        doReturn(file).when(artifact2).getFile();

        createPluginXmlFile(groupId, artifactId1, version2);
        file = createJarFile(artifactId1 + "-" + version2);
        doReturn(file).when(artifact3).getFile();

        createPluginXmlFile(groupId, artifactId2, version2);
        file = createJarFile(artifactId2 + "-" + version2);
        doReturn(file).when(artifact4).getFile();

        // Artifacts
        createPluginXmlFile(groupId, artifactId3, version1);
        file = createJarFile(artifactId3 + "-" + version1);
        doReturn(file).when(artifact5).getFile();

        createPluginXmlFile(groupId, artifactId3, version2);
        file = createJarFile(artifactId3 + "-" + version2);
        doReturn(file).when(artifact6).getFile();

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

        PluginArtifact pluginArtifact1 = new PluginArtifact(p1, artifact1);
        PluginArtifact pluginArtifact2 = new PluginArtifact(p2, artifact2);
        PluginArtifact pluginArtifact3 = new PluginArtifact(p3, artifact3);
        PluginArtifact pluginArtifact4 = new PluginArtifact(p4, artifact4);

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(Paths.get(propertiesBooter.getTempDirectory()));

        // When
        artifactDeployer.generateAndDeployArtifact(pluginArtifact1, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(pluginArtifact2, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(pluginArtifact3, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(pluginArtifact4, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact5, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact6, storageId, repositoryId);

        // Then
        // Group level metadata
        String metadataPath = "storages/" + storageId + "/" + repositoryId + "/" + getGroupLevelMetadataPath(artifact1);
        Metadata groupLevelMetadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(groupLevelMetadata);
        assertEquals(2, groupLevelMetadata.getPlugins().size());

        // Artifact Level metadata
        metadataPath = "storages/" + storageId + "/" + repositoryId + "/" + getArtifactLevelMetadataPath(artifact1);
        Metadata artifactLevelMetadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId1, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        metadataPath = "storages/" + storageId + "/" + repositoryId + "/" + getArtifactLevelMetadataPath(artifact2);
        artifactLevelMetadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId2, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        metadataPath = "storages/" + storageId + "/" + repositoryId + "/" + getArtifactLevelMetadataPath(artifact5);
        artifactLevelMetadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(artifactLevelMetadata);
        assertEquals(groupId, artifactLevelMetadata.getGroupId());
        assertEquals(artifactId3, artifactLevelMetadata.getArtifactId());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testUpdateMetadataOnDeleteReleaseVersionDirectory(@MavenRepository(repositoryId = REPOSITORY_RELEASES2,
                                                                                   setup = MavenIndexedRepositorySetup.class)
                                                                  Repository repository,
                                                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES2,
                                                                                     id = "org.carlspring.strongbox.delete-metadata:metadata-foo",
                                                                                     versions = { "1.2.1",
                                                                                                  "1.2.2" },
                                                                                     classifiers = "javadoc")
                                                                  List<Path> artifactsPaths)
            throws Exception
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath artifact1RepositoryPath = (RepositoryPath) artifactsPaths.get(0).normalize();
        String artifact1RepositoryPathStr = RepositoryFiles.relativizePath(artifact1RepositoryPath);
        String unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifact1RepositoryPathStr);
        Artifact artifact1 = MavenArtifactUtils.convertPathToArtifact(unixBasedRelativePath);

        RepositoryPath artifact2RepositoryPath = (RepositoryPath) artifactsPaths.get(1).normalize();
        String artifact2RepositoryPathStr = RepositoryFiles.relativizePath(artifact2RepositoryPath);

        // When
        mavenMetadataServiceHelper.generateMavenMetadata(repository);

        client.delete(storageId, repositoryId, artifact2RepositoryPathStr);

        // Then
        assertNotNull(artifact1);
        String metadataPath = String.format("storages/%s/%s/%s",
                                            storageId,
                                            repositoryId,
                                            getArtifactLevelMetadataPath(artifact1));
        Metadata metadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

        assertNotNull(metadata);
        assertNotNull(metadata.getVersioning());
        assertNotNull(metadata.getVersioning().getVersions());
        assertFalse(metadata.getVersioning()
                            .getVersions()
                            .contains("1.2.2"));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testUpdateMetadataOnDeleteSnapshotVersionDirectory(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                                    policy = RepositoryPolicyEnum.SNAPSHOT)
                                                                   Repository repository,
                                                                   @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                                      id = "org.carlspring.strongbox.metadata:metadata-foo",
                                                                                      versions = { "3.1-20190812.124500-1",
                                                                                                   "3.1-20190812.124600-2",
                                                                                                   "3.1-20190812.124700-3",
                                                                                                   "3.1-20190812.124800-4" })
                                                                   List<Path> artifactsPaths)
            throws XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        mavenMetadataServiceHelper.generateMavenMetadata(repository);

        RepositoryPath artifact1RepositoryPath = (RepositoryPath) artifactsPaths.get(0).normalize();
        String artifact1RepositoryPathStr = RepositoryFiles.relativizePath(artifact1RepositoryPath);
        String unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifact1RepositoryPathStr);
        Artifact artifact1 = MavenArtifactUtils.convertPathToArtifact(unixBasedRelativePath);

        RepositoryPath artifact1ParentPath = artifact1RepositoryPath.getParent();
        String artifact1ParentPathStr = RepositoryFiles.relativizePath(artifact1ParentPath);

        // When
        client.delete(storageId, repositoryId, artifact1ParentPathStr);

        // Then
        assertNotNull(artifact1);
        String metadataPath = String.format("storages/%s/%s/%s",
                                            storageId,
                                            repositoryId,
                                            getArtifactLevelMetadataPath(artifact1));
        Metadata metadata = defaultMavenArtifactDeployer.retrieveMetadata(metadataPath);

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
     * User deployer does not have general the ARTIFACTS_RESOLVE permission, but it's defined for single 'releases'
     * repository. So because of dynamic privileges assignment they will be able to get access to artifacts in that
     * repository.
     */
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @WithMockUser(username = "deployer", authorities = "ARTIFACTS_RESOLVE")
    public void testDynamicPrivilegeAssignmentForRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES1)
                                                            Repository repository,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES1,
                                                                               id = "org.carlspring.strongbox.test:dynamic-privileges",
                                                                               versions = "1.0")
                                                            Path artifactPath)
            throws IOException
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";

        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);

        // When
        int statusCode = given().header(HttpHeaders.USER_AGENT, "Maven/*")
                                .contentType(MediaType.TEXT_PLAIN_VALUE)
                                .when()
                                .get(url, storageId, repositoryId, artifactRepositoryPathStr)
                                .getStatusCode();

        // Then
        assertEquals(HttpStatus.OK.value(), statusCode,
                     "Access was wrongly restricted for user with custom access model");
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

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";
        String artifactPath = String.format("org/carlspring/commons/commons-http/%s/commons-http-%s.jar",
                                            commonsHttpSnapshot.version, commonsHttpSnapshot.timestampedVersion);

        given().header(HttpHeaders.USER_AGENT, "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url, "public", "maven-group", artifactPath)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldNotAllowRequestingPathsWithSlashAtTheEnd()
    {
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";
        given().when()
               .get(url, "public", "maven-group", "org/carlspring/commons/commons-io/")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .statusLine(equalTo("400 The specified path should not ends with `/` character!"));
    }

    @Test
    public void shouldRequireArtifactVersion()
    {
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";
        given().when()
               .get(url, "public", "maven-group", "org/carlspring/logging/logback-configuration")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .statusLine(equalTo("400 The specified path is invalid. Maven GAV not recognized."));
    }

    @Test
    public void testNonExistingArtifactInNonExistingDirectory()
    {
        String path = "/storages/storage-common-proxies/maven-central/john/doe/who.jar";
        ExtractableResponse response = client.getResourceWithResponse(path, "");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Wrong response");
    }

    @Test
    public void shouldNotAllowGettingExistingDirectories()
            throws IOException
    {
        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository(STORAGE0,
                                                                             REPOSITORY_RELEASES);

        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        String artifactParentPathStr = "org/carlspring/logging/logback-configuration-core";
        Path artifactParentPath = repositoryPath.resolve(artifactParentPathStr);

        Files.createDirectories(artifactParentPath);

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";

        given().when()
               .get(url, repository.getStorage().getId(), repository.getId(), artifactParentPathStr)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .statusLine(equalTo("400 The specified path is a directory!"));

        Files.delete(artifactParentPath);
        Files.delete(artifactParentPath.getParent());
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

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";
        String artifactPath = String.format("org/carlspring/commons/commons-http/%s/commons-http-%s.jar",
                                            commonsHttpSnapshot.version, commonsHttpSnapshot.timestampedVersion);

        given().header(HttpHeaders.USER_AGENT, "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url, "storage-common-proxies", "carlspring", artifactPath)
               .then()
               .statusCode(HttpStatus.OK.value());

        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact("storage-common-proxies",
                                                                           "carlspring",
                                                                           artifactPath);
        assertNotNull(artifactEntry);
        assertNotNull(artifactEntry.getArtifactCoordinates());

        assertTrue(artifactEntry instanceof RemoteArtifactEntry);
        assertTrue(((RemoteArtifactEntry) artifactEntry).getIsCached());
    }

    private ArtifactSnapshotVersion getCommonsHttpArtifactSnapshotVersionFromCarlspringRemote()
            throws Exception
    {

        Metadata libraryMetadata = defaultMavenArtifactDeployer.retrieveMetadata(
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

        Metadata artifactMetadata = defaultMavenArtifactDeployer.retrieveMetadata(
                "/storages/storage-common-proxies/carlspring/" +
                "org/carlspring/commons/commons-http/" +
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
