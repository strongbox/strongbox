package org.carlspring.strongbox.controllers.layout.maven;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenMetadataManagementControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "mmct-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mmct-snapshots";

    private static final Path REPOSITORY_BASEDIR_SNAPSHOTS = Paths.get(ConfigurationResourceResolver.getVaultDirectory())
                                                                  .resolve("storages")
                                                                  .resolve(STORAGE0)
                                                                  .resolve(REPOSITORY_SNAPSHOTS);

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        // Create repositories
        MutableRepository repositoryReleases = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repositoryReleases.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(STORAGE0, repositoryReleases);

        MutableRepository repositorySnapshots = mavenRepositoryFactory.createRepository(REPOSITORY_SNAPSHOTS);
        repositorySnapshots.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());

        createRepository(STORAGE0, repositorySnapshots);

        // Generate artifacts
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(),
                         "org.carlspring.strongbox.metadata:strongbox-metadata",
                         new String[]{ "3.0.1",
                                       "3.0.2",
                                       "3.1",
                                       "3.2" });

        // Generate snapshots
        String snapshotPath = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();

        createTimestampedSnapshotArtifact(snapshotPath,
                                          "org.carlspring.strongbox.metadata",
                                          "strongbox-metadata",
                                          "3.0.1",
                                          "jar",
                                          null,
                                          3);

        createTimestampedSnapshotArtifact(snapshotPath,
                                          "org.carlspring.strongbox.metadata",
                                          "strongbox-metadata",
                                          "3.0.2",
                                          "jar",
                                          null,
                                          4);

        createTimestampedSnapshotArtifact(snapshotPath,
                                          "org.carlspring.strongbox.metadata",
                                          "strongbox-metadata",
                                          "3.1",
                                          "jar",
                                          null,
                                          5);
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testRebuildSnapshotMetadata()
            throws Exception
    {
        String metadataPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS +
                              "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        String url = getContextBaseUrl() + metadataPath;

        client.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, null);

        given().header("user-agent", "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

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
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.toString(),
                                          "org.carlspring.strongbox.metadata.foo",
                                          "strongbox-metadata-bar",
                                          "1.2.3",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.toString(),
                                          "org.carlspring.strongbox.metadata.foo.bar",
                                          "strongbox-metadata-foo",
                                          "2.1",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.toString(),
                                          "org.carlspring.strongbox.metadata.foo.bar",
                                          "strongbox-metadata-foo-bar",
                                          "5.4",
                                          "jar",
                                          null,
                                          4);

        String metadataUrl = "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS + "/org/carlspring/strongbox/metadata";
        String metadataPath1 = metadataUrl + "/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = metadataUrl + "/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
        String metadataPath2Snapshot = metadataUrl + "/foo/bar/strongbox-metadata-foo/2.1-SNAPSHOT/maven-metadata.xml";
        String metadataPath3 = metadataUrl + "/foo/bar/strongbox-metadata-foo-bar/maven-metadata.xml";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath1));
        assertFalse("Metadata already exists!", client.pathExists(metadataPath2));
        assertFalse("Metadata already exists!", client.pathExists(metadataPath3));

        client.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/metadata/foo/bar");

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

        logger.info("[testRebuildSnapshotMetadataWithBasePath] latestTimestamp " + latestTimestamp);

        client.removeVersionFromMetadata(STORAGE0,
                                         REPOSITORY_SNAPSHOTS,
                                         "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo",
                                         latestTimestamp,
                                         "",
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

        assertNotNull("Incorrect metadata!", metadata2SnapshotAfter.getVersioning());
        assertFalse("Failed to remove timestamped SNAPSHOT version!",
                    MetadataHelper.containsVersion(metadata2SnapshotAfter, latestTimestamp));
        assertEquals("Incorrect metadata!", timestamp,
                     metadata2SnapshotAfter.getVersioning().getSnapshot().getTimestamp());
        assertEquals("Incorrect metadata!", Integer.parseInt(buildNumber),
                     metadata2SnapshotAfter.getVersioning().getSnapshot().getBuildNumber());
        assertEquals("Incorrect metadata!", previousLatestTimestamp,
                     metadata2AfterSnapshotVersions.get(metadata2AfterSnapshotVersions.size() - 1).getVersion());
    }

    @Test
    public void testRebuildReleaseMetadataAndDeleteAVersion()
            throws Exception
    {
        String metadataPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES +
                              "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        assertFalse("Metadata already exists!", client.pathExists(metadataPath));

        // create new metadata
        client.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, null);

        assertTrue("Failed to rebuild release metadata!", client.pathExists(metadataPath));

        InputStream is = client.getResource(metadataPath);
        Metadata metadataBefore = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning());
        assertNotNull("Incorrect metadata!", metadataBefore.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.2", metadataBefore.getVersioning().getLatest());

        client.removeVersionFromMetadata(STORAGE0,
                                         REPOSITORY_RELEASES,
                                         artifactPath,
                                         "3.2",
                                         null,
                                         MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        is = client.getResource(metadataPath);
        Metadata metadataAfter = artifactMetadataService.getMetadata(is);

        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning());
        assertFalse("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "3.2"));
        assertNotNull("Incorrect metadata!", metadataAfter.getVersioning().getLatest());
        assertEquals("Incorrect metadata!", "3.1", metadataAfter.getVersioning().getLatest());
    }

}
