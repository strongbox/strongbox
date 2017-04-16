package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenMetadataManagementControllerTest
        extends RestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "mmct-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mmct-snapshots";
    
    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);
    private static final File REPOSITORY_BASEDIR_SNAPSHOTS = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

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
        // Create repositories
        Storage storage = configurationManager.getConfiguration() .getStorage(STORAGE0);

        Repository repositoryReleases = new Repository(REPOSITORY_RELEASES);
        repositoryReleases.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repositoryReleases.setStorage(storage);

        createRepository(repositoryReleases);

        Repository repositorySnapshots = new Repository(REPOSITORY_SNAPSHOTS);
        repositorySnapshots.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
        repositorySnapshots.setStorage(storage);

        createRepository(repositorySnapshots);

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

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS));

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
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                          "org.carlspring.strongbox.metadata.foo",
                                          "strongbox-metadata-bar",
                                          "1.2.3",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
                                          "org.carlspring.strongbox.metadata.foo.bar",
                                          "strongbox-metadata-foo",
                                          "2.1",
                                          "jar",
                                          null,
                                          5);
        createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_SNAPSHOTS.getAbsolutePath(),
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
