package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class MavenMetadataManagementControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "mmct-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mmct-snapshots";

    @Inject
    private ArtifactMetadataService artifactMetadataService;


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRebuildSnapshotMetadata(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS)
                                            Repository repository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                               id = "org.carlspring.strongbox.metadata:strongbox-metadata",
                                                               versions = { "3.0.1-20190716.202015-1",
                                                                            "3.0.1-20190716.202101-2",
                                                                            "3.0.1-20190716.202203-3",
                                                                            "3.0.2-20190716.202315-1",
                                                                            "3.0.2-20190716.202401-2",
                                                                            "3.0.2-20190716.202503-3",
                                                                            "3.0.2-20190716.202615-4",
                                                                            "3.1-20190716.202701-1",
                                                                            "3.1-20190716.202803-2",
                                                                            "3.1-20190716.202915-3",
                                                                            "3.1-20190716.203001-4",
                                                                            "3.1-20190716.203103-5" })
                                            List<Path> repositoryArtifacts)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String metadataPath = "/storages/" + storageId + "/" + repositoryId +
                              "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";

        assertFalse(client.pathExists(metadataPath), "Metadata already exists!");
        client.rebuildMetadata(storageId, repositoryId, null);

        String url = getContextBaseUrl() + metadataPath;
        given().contentType(MediaType.TEXT_PLAIN_VALUE)
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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRebuildSnapshotMetadataWithBasePath(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS)
                                                        Repository repository,
                                                        @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                           id = "org.carlspring.strongbox.metadata.foo:strongbox-metadata-bar",
                                                                           versions = { "1.2.3-20190716.202015-1",
                                                                                        "1.2.3-20190716.202101-2",
                                                                                        "1.2.3-20190716.202203-3",
                                                                                        "1.2.3-20190716.202303-4",
                                                                                        "1.2.3-20190716.202403-5" })
                                                        List<Path> repositoryArtifacts1,
                                                        @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                           id = "org.carlspring.strongbox.metadata.foo.bar:strongbox-metadata-foo",
                                                                           versions = { "2.1-20190716.202515-1",
                                                                                        "2.1-20190716.202601-2",
                                                                                        "2.1-20190716.202703-3",
                                                                                        "2.1-20190716.202815-4",
                                                                                        "2.1-20190716.202915-5" })
                                                        List<Path> repositoryArtifacts2,
                                                        @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                           id = "org.carlspring.strongbox.metadata.foo.bar:strongbox-metadata-foo-bar",
                                                                           versions = { "5.4-20190716.203001-1",
                                                                                        "5.4-20190716.203103-2",
                                                                                        "5.4-20190716.203215-3",
                                                                                        "5.4-20190716.203301-4" })
                                                        List<Path> repositoryArtifacts3)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String metadataUrl = "/storages/" + storageId + "/" + repositoryId + "/org/carlspring/strongbox/metadata";
        String metadataPath1 = metadataUrl + "/foo/strongbox-metadata-bar/maven-metadata.xml";
        String metadataPath2 = metadataUrl + "/foo/bar/strongbox-metadata-foo/maven-metadata.xml";
        String metadataPath2Snapshot = metadataUrl + "/foo/bar/strongbox-metadata-foo/2.1-SNAPSHOT/maven-metadata.xml";
        String metadataPath3 = metadataUrl + "/foo/bar/strongbox-metadata-foo-bar/maven-metadata.xml";

        assertFalse(client.pathExists(metadataPath1), "Metadata already exists!");
        assertFalse(client.pathExists(metadataPath2), "Metadata already exists!");
        assertFalse(client.pathExists(metadataPath3), "Metadata already exists!");

        client.rebuildMetadata(storageId, repositoryId, "org/carlspring/strongbox/metadata/foo/bar");

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

        logger.debug("[testRebuildSnapshotMetadataWithBasePath] latestTimestamp {}", latestTimestamp);

        client.removeVersionFromMetadata(storageId,
                                         repositoryId,
                                         "org/carlspring/strongbox/metadata/foo/bar/strongbox-metadata-foo",
                                         latestTimestamp,
                                         "",
                                         MetadataType.ARTIFACT_ROOT_LEVEL.getType());

        is = client.getResource(metadataPath2Snapshot);
        Metadata metadata2SnapshotAfter = artifactMetadataService.getMetadata(is);
        List<SnapshotVersion> metadata2AfterSnapshotVersions = metadata2SnapshotAfter.getVersioning().getSnapshotVersions();

        String timestamp = previousLatestTimestamp.substring(previousLatestTimestamp.indexOf('-') + 1,
                                                             previousLatestTimestamp.lastIndexOf('-'));
        String buildNumber = previousLatestTimestamp.substring(previousLatestTimestamp.lastIndexOf('-') + 1);

        logger.debug("\n\tpreviousLatestTimestamp {}\n\ttimestamp {}\n\tbuildNumber {}", previousLatestTimestamp,
                                                                                        timestamp,
                                                                                        buildNumber);

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


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRebuildReleaseMetadataAndDeleteAVersion(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                                            Repository repository,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                               id = "org.carlspring.strongbox.metadata:strongbox-metadata",
                                                                               versions = { "3.1",
                                                                                            "3.2" })
                                                            List<Path> repositoryArtifacts)
            throws Exception
    {

        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String metadataPath = "/storages/" + storageId + "/" + repositoryId +
                              "/org/carlspring/strongbox/metadata/strongbox-metadata/maven-metadata.xml";
        String artifactPath = "org/carlspring/strongbox/metadata/strongbox-metadata";

        assertFalse(client.pathExists(metadataPath), "Metadata already exists!");

        // create new metadata
        client.rebuildMetadata(storageId, repositoryId, null);

        assertTrue(client.pathExists(metadataPath), "Failed to rebuild release metadata!");

        InputStream is = client.getResource(metadataPath);
        Metadata metadataBefore = artifactMetadataService.getMetadata(is);

        assertNotNull(metadataBefore.getVersioning(), "Incorrect metadata!");
        assertNotNull(metadataBefore.getVersioning().getLatest(), "Incorrect metadata!");
        assertEquals("3.2", metadataBefore.getVersioning().getLatest(), "Incorrect metadata!");

        client.removeVersionFromMetadata(storageId,
                                         repositoryId,
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

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenRepository(policy = RepositoryPolicyEnum.SNAPSHOT)
    private @interface MavenSnapshotRepository
    {
        @AliasFor(annotation = MavenRepository.class, attribute = "repositoryId")
        String value();
    }
}
