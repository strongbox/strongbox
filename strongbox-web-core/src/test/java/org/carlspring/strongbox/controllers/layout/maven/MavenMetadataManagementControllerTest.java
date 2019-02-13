package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@SpringBootTest
public class MavenMetadataManagementControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "mmct-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mmct-snapshots";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

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

    @AfterEach
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

        assertFalse(client.pathExists(metadataPath), "Metadata already exists!");

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

        assertNotNull(metadata.getVersioning(), "Incorrect metadata!");
        assertNotNull(metadata.getVersioning().getLatest(), "Incorrect metadata!");
    }

    @Test
    public void testRebuildSnapshotMetadataWithBasePath()
            throws Exception
    {
        // Generate snapshots in nested dirs
        createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
                                          "org.carlspring.strongbox.metadata.foo",
                                          "strongbox-metadata-bar",
                                          "1.2.3",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
                                          "org.carlspring.strongbox.metadata.foo.bar",
                                          "strongbox-metadata-foo",
                                          "2.1",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
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

        assertFalse(client.pathExists(metadataPath1), "Metadata already exists!");
        assertFalse(client.pathExists(metadataPath2), "Metadata already exists!");
        assertFalse(client.pathExists(metadataPath3), "Metadata already exists!");

        client.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/metadata/foo/bar");

        assertFalse(client.pathExists(metadataPath1), "Failed to rebuild snapshot metadata!");
        assertTrue(client.pathExists(metadataPath2), "Failed to rebuild snapshot metadata!");
        assertTrue(client.pathExists(metadataPath3), "Failed to rebuild snapshot metadata!");

        InputStream is = client.getResource(metadataPath2);
        Metadata metadata2 = artifactMetadataService.getMetadata(is);

        assertNotNull(metadata2.getVersioning(), "Incorrect metadata!");
        assertNotNull(metadata2.getVersioning().getLatest(), "Incorrect metadata!");

        is = client.getResource(metadataPath3);
        Metadata metadata3 = artifactMetadataService.getMetadata(is);

        assertNotNull(metadata3.getVersioning(), "Incorrect metadata!");
        assertNotNull(metadata3.getVersioning().getLatest(), "Incorrect metadata!");

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

        assertNotNull(metadata2SnapshotAfter.getVersioning(), "Incorrect metadata!");
        assertFalse(MetadataHelper.containsVersion(metadata2SnapshotAfter, latestTimestamp),
                    "Failed to remove timestamped SNAPSHOT version!");
        assertEquals(timestamp, metadata2SnapshotAfter.getVersioning().getSnapshot().getTimestamp(),
                     "Incorrect metadata!");
        assertEquals(Integer.parseInt(buildNumber), metadata2SnapshotAfter.getVersioning().getSnapshot().getBuildNumber(),
                     "Incorrect metadata!");
        assertEquals(previousLatestTimestamp, metadata2AfterSnapshotVersions.get(metadata2AfterSnapshotVersions.size() - 1).getVersion(),
                     "Incorrect metadata!");
    }

    @Test
    public void testRebuildReleaseMetadataAndDeleteAVersion()
            throws Exception
    {
        String metadataPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES +
                              "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        assertFalse(client.pathExists(metadataPath), "Metadata already exists!");

        // create new metadata
        client.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, null);

        assertTrue(client.pathExists(metadataPath), "Failed to rebuild release metadata!");

        InputStream is = client.getResource(metadataPath);
        Metadata metadataBefore = artifactMetadataService.getMetadata(is);

        assertNotNull(metadataBefore.getVersioning(), "Incorrect metadata!");
        assertNotNull(metadataBefore.getVersioning().getLatest(), "Incorrect metadata!");
        assertEquals("3.2", metadataBefore.getVersioning().getLatest(), "Incorrect metadata!");

        client.removeVersionFromMetadata(STORAGE0,
                                         REPOSITORY_RELEASES,
                                         artifactPath,
                                         "3.2",
                                         null,
                                         MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        is = client.getResource(metadataPath);
        Metadata metadataAfter = artifactMetadataService.getMetadata(is);

        assertNotNull(metadataAfter.getVersioning(), "Incorrect metadata!");
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "3.2"), "Unexpected set of versions!");
        assertNotNull(metadataAfter.getVersioning().getLatest(), "Incorrect metadata!");
        assertEquals("3.1", metadataAfter.getVersioning().getLatest(), "Incorrect metadata!");
    }

}
