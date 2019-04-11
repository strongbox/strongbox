package org.carlspring.strongbox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactWithClassifiers;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author stodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactMetadataServiceSnapshotsTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_SNAPSHOTS = "amss-snapshots";

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private Calendar calendar = Calendar.getInstance();


//    @Test
//    public void testSnapshotMetadataRebuild()
//            throws IOException, XmlPullParserException, NoSuchAlgorithmException
//    {
//        // Create snapshot artifacts
//        MavenArtifact artifact = createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
//                                                                   "org.carlspring.strongbox",
//                                                                   "strongbox-metadata",
//                                                                   "2.0",
//                                                                   "jar",
//                                                                   CLASSIFIERS,
//                                                                   5);
//
//        changeCreationDate(artifact);
//
//        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, ARTIFACT_BASE_PATH_STRONGBOX_METADATA);
//
//        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
//                                                                REPOSITORY_SNAPSHOTS,
//                                                                "org/carlspring/strongbox/strongbox-metadata");
//
//        assertNotNull(metadata);
//
//        Versioning versioning = metadata.getVersioning();
//
//        assertEquals(artifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
//        assertEquals(artifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
//
//        assertNotNull(versioning.getVersions().size(), "No versioning information could be found in the metadata!");
//        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
//    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testDeleteVersionFromMetadata(@MavenSnapshotRepository Repository repository,
                                              @MavenSnapshotArtifactWithClassifiers(id = "org.carlspring.strongbox:deleted", versions = { "1.0-20180328.195810-1",
                                                                                                                                          "1.0-20180328.195810-2",
                                                                                                                                          "1.0-20180328.195810-3",
                                                                                                                                          "1.0-20180328.195810-4",
                                                                                                                                          "1.0-20180328.195810-5" }) List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.0-SNAPSHOT"), "Unexpected set of versions!");

        artifactMetadataService.removeVersion(STORAGE0,
                                              REPOSITORY_SNAPSHOTS,
                                              artifactPath,
                                              "1.0-SNAPSHOT",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "1.0-SNAPSHOT"), "Unexpected set of versions!");
    }

//    @Test
//    public void testAddTimestampedSnapshotVersionToMetadata()
//            throws IOException, XmlPullParserException, NoSuchAlgorithmException
//    {
//        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();
//
//        // Create snapshot artifacts
//        MavenArtifact addedArtifact = createTimestampedSnapshotArtifact(repositoryBasedir,
//                                                                        "org.carlspring.strongbox",
//                                                                        "added",
//                                                                        "2.0",
//                                                                        "jar",
//                                                                        CLASSIFIERS,
//                                                                        5);
//
//        changeCreationDate(addedArtifact);
//
//        String artifactPath = "org/carlspring/strongbox/added";
//
//        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);
//
//        String metadataPath = artifactPath + "/2.0-SNAPSHOT";
//
//        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);
//
//        assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, addedArtifact.getVersion(), null));
//
//        String timestamp = formatter.format(calendar.getTime());
//        String version = "2.0-" + timestamp + "-" + 6;
//
//        addedArtifact = new MavenRepositoryArtifact(addedArtifact.getGroupId(),
//                                                    addedArtifact.getArtifactId(),
//                                                    version);
//
//        artifactMetadataService.addTimestampedSnapshotVersion(STORAGE0,
//                                                              REPOSITORY_SNAPSHOTS,
//                                                              artifactPath,
//                                                              addedArtifact.getVersion(),
//                                                              null,
//                                                              "jar");
//
//        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);
//
//        assertNotNull(metadataAfter);
//        assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, addedArtifact.getVersion()),
//                   "Failed to add timestamped SNAPSHOT version to metadata!");
//    }

//    @Test
//    public void testDeleteTimestampedSnapshotVersionFromMetadata()
//            throws IOException, XmlPullParserException, NoSuchAlgorithmException
//    {
//        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();
//
//        // Create snapshot artifacts
//        MavenArtifact deletedArtifact = createTimestampedSnapshotArtifact(repositoryBasedir,
//                                                                          "org.carlspring.strongbox",
//                                                                          "deleted",
//                                                                          "2.0",
//                                                                          "jar",
//                                                                          CLASSIFIERS,
//                                                                          5);
//
//        changeCreationDate(deletedArtifact);
//
//        String artifactPath = "org/carlspring/strongbox/deleted";
//
//        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);
//
//        String metadataPath = artifactPath + "/2.0-SNAPSHOT";
//
//        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);
//
//        assertTrue(
//                MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, deletedArtifact.getVersion(), null));
//
//        artifactMetadataService.removeTimestampedSnapshotVersion(STORAGE0,
//                                                                 REPOSITORY_SNAPSHOTS,
//                                                                 artifactPath,
//                                                                 deletedArtifact.getVersion(),
//                                                                 null);
//
//        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);
//
//        assertNotNull(metadataAfter);
//        assertFalse(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, deletedArtifact.getVersion()));
//    }

    @Test
    public void testSnapshotWithoutTimestampMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String version = "2.0-SNAPSHOT";

        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();

        generateArtifact(repositoryBasedir,
                         "org.carlspring.strongbox.snapshots:metadata:" + version);

        final String artifactPath = "org/carlspring/strongbox/snapshots/metadata";

        MavenArtifact snapshotArtifact = new MavenRepositoryArtifact("org.carlspring.strongbox.snapshots",
                                                                     "metadata",
                                                                     version);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();
        Versioning snapshotVersioning = snapshotMetadata.getVersioning();

        assertEquals(snapshotArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(snapshotArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");

        assertNotNull(versioning.getVersions(), "No versioning information could be found in the metadata!");
        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
        assertEquals(version, versioning.getLatest());
        assertNotNull(versioning.getLastUpdated(), "Failed to set lastUpdated field!");

        assertNotNull(snapshotVersioning.getVersions(),
                      "No versioning information could be found in the metadata!");
        assertEquals(1, snapshotVersioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
        assertEquals(version, snapshotVersioning.getLatest());
        assertNotNull(snapshotVersioning.getLastUpdated(), "Failed to set lastUpdated field!");
    }

    @Test
    public void testSnapshotPluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create plugin artifact
        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();

        MavenArtifact pluginArtifact = createTimestampedSnapshotArtifact(repositoryBasedir,
                                                                         "org.carlspring.strongbox.maven",
                                                                         "strongbox-metadata-plugin",
                                                                         "1.1",
                                                                         "maven-plugin",
                                                                         null);

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(pluginArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(pluginArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        assertNull(versioning.getRelease(), "Incorrect latest release version!");

        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

//    @Test
//    public void testMetadataMerge()
//            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
//    {
//        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();
//
//        // Create an artifact for metadata merging tests
//        MavenArtifact mergeArtifact = createTimestampedSnapshotArtifact(repositoryBasedir,
//                                                                        "org.carlspring.strongbox",
//                                                                        "strongbox-metadata-merge",
//                                                                        "2.0",
//                                                                        CLASSIFIERS);
//
//        // Generate a proper maven-metadata.xml
//        artifactMetadataService.rebuildMetadata(STORAGE0,
//                                                REPOSITORY_SNAPSHOTS,
//                                                "org/carlspring/strongbox/strongbox-metadata-merge");
//
//        // Generate metadata to merge
//        Metadata mergeMetadata = new Metadata();
//        Versioning appendVersioning = new Versioning();
//
//        appendVersioning.addVersion("1.0-SNAPSHOT");
//        appendVersioning.addVersion("1.3-SNAPSHOT");
//
//        Snapshot snapshot = new Snapshot();
//        snapshot.setTimestamp(formatter.format(calendar.getTime()));
//        snapshot.setBuildNumber(1);
//
//        appendVersioning.setRelease(null);
//        appendVersioning.setSnapshot(snapshot);
//        appendVersioning.setLatest("1.3-SNAPSHOT");
//
//        mergeMetadata.setVersioning(appendVersioning);
//
//        // Merge
//        artifactMetadataService.mergeMetadata(mergeArtifact, mergeMetadata);
//
//        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
//                                                                REPOSITORY_SNAPSHOTS,
//                                                                "org/carlspring/strongbox/strongbox-metadata-merge");
//
//        assertNotNull(metadata);
//
//        assertEquals("1.3-SNAPSHOT", metadata.getVersioning().getLatest(), "Incorrect latest release version!");
//        assertEquals(3, metadata.getVersioning().getVersions().size(),
//                     "Incorrect number of versions stored in metadata!");
//    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @TestRepository(layout = MavenArtifactCoordinates.LAYOUT_NAME, repository = REPOSITORY_SNAPSHOTS)
    private static @interface MavenSnapshotRepository
    {

    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenArtifactWithClassifiers(repository = REPOSITORY_SNAPSHOTS)
    private static @interface MavenSnapshotArtifactWithClassifiers
    {

        /**
         * The artifact "GA" (ex. "org.carlspring.test:test-artifact").
         */
        @AliasFor(annotation = MavenArtifactWithClassifiers.class)
        String id() default "";

        /**
         * The {@link MavenArtifactCoordinates} versions.
         */
        @AliasFor(annotation = MavenArtifactWithClassifiers.class)
        String[] versions() default {};
        
    }
    
}
