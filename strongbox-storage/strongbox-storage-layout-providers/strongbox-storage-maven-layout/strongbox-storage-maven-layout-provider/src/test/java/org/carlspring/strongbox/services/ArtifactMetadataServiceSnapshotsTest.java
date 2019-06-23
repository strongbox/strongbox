package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author stodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactMetadataServiceSnapshotsTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String TIMESTAMPED_SNAPSHOT_VERSION_FORMAT = "yyyyMMdd.HHmmss";

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testSnapshotMetadataRebuild(@MavenSnapshotRepository("smr") Repository repository,
                                            @MavenSnapshotArtifactsWithClassifiers(repositoryId = "smr", id = "org.carlspring.strongbox:strongbox-metadata") List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata(STORAGE0, repository.getId(), ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repository.getId(),
                                                                "org/carlspring/strongbox/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("strongbox-metadata", metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals("org.carlspring.strongbox", metadata.getGroupId(), "Incorrect groupId!");

        assertNotNull(versioning.getVersions().size(), "No versioning information could be found in the metadata!");
        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testDeleteVersionFromMetadata(@MavenSnapshotRepository("dvfm") Repository repository,
                                              @MavenSnapshotArtifactsWithClassifiers(repositoryId = "dvfm",  id = "org.carlspring.strongbox:deleted") List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, repository.getId(), artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.0-SNAPSHOT"), "Unexpected set of versions!");

        artifactMetadataService.removeVersion(STORAGE0,
                                              repository.getId(),
                                              artifactPath,
                                              "1.0-SNAPSHOT",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), artifactPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "1.0-SNAPSHOT"), "Unexpected set of versions!");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testAddTimestampedSnapshotVersionToMetadata(@MavenSnapshotRepository("atsvtm") Repository repository,
                                                            @MavenSnapshotArtifactsWithClassifiers(repositoryId = "atsvtm", id = "org.carlspring.strongbox:added") List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        

        String artifactPath = "org/carlspring/strongbox/added";
        artifactMetadataService.rebuildMetadata(STORAGE0, repository.getId(), artifactPath);

        String metadataPath = artifactPath + "/1.0-SNAPSHOT";
        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), metadataPath);
        for (Path path : snapshotArtifacts)
        {
            MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath)path.normalize());
            assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, coordinates.getVersion(), null));
        }

        SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMPED_SNAPSHOT_VERSION_FORMAT);
        String timestamp = formatter.format(Calendar.getInstance().getTime());
        String version = "1.0-" + timestamp + "-" + 6;

        MavenRepositoryArtifact addedArtifact = new MavenRepositoryArtifact(new Gav("org.carlspring.strongbox",
                                                                                    "strongbox-added",
                                                                                    version));

        artifactMetadataService.addTimestampedSnapshotVersion(STORAGE0,
                                                              repository.getId(),
                                                              artifactPath,
                                                              addedArtifact.getVersion(),
                                                              null,
                                                              "jar");

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), metadataPath);

        assertNotNull(metadataAfter);
        assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, addedArtifact.getVersion()),
                   "Failed to add timestamped SNAPSHOT version to metadata!");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testDeleteTimestampedSnapshotVersionFromMetadata(@MavenSnapshotRepository("dtsvfm") Repository repository,
                                                                 @MavenSnapshotArtifactsWithClassifiers(repositoryId = "dtsvfm", id = "org.carlspring.strongbox:deleted") List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, repository.getId(), artifactPath);

        String metadataPath = artifactPath + "/1.0-SNAPSHOT";

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), metadataPath);
        for (Path path : snapshotArtifacts)
        {
            MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath)path.normalize());
            assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, coordinates.getVersion(), null));
        }

        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath)snapshotArtifacts.iterator().next().normalize());
        artifactMetadataService.removeTimestampedSnapshotVersion(STORAGE0,
                                                                 repository.getId(),
                                                                 artifactPath,
                                                                 coordinates.getVersion(),
                                                                 null);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), metadataPath);
        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, coordinates.getVersion()));
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testSnapshotWithoutTimestampMetadataRebuild(@MavenSnapshotRepository("swtmr") Repository repository,
                                                            @MavenArtifactWithClassifiers(repositoryId = "swtmr", id = "org.carlspring.strongbox.snapshots:metadata", versions = "2.0-SNAPSHOT") Path snapshotArtifactPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String version = "2.0-SNAPSHOT";

        final String artifactPath = "org/carlspring/strongbox/snapshots/metadata";

        MavenArtifact snapshotArtifact = new MavenRepositoryArtifact(new Gav("org.carlspring.strongbox.snapshots",
                                                                             "metadata",
                                                                             version));

        artifactMetadataService.rebuildMetadata(STORAGE0, repository.getId(), artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata(STORAGE0, repository.getId(), artifactPath);

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
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testSnapshotPluginMetadataRebuild(@MavenSnapshotRepository("spmr") Repository repository,
                                                  @MavenPluginArtifact(repositoryId = "spmr",  id = "org.carlspring.strongbox.maven:strongbox-metadata-plugin", versions = "1.1-20180328.195810-1") Path pluginSnapshotPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                repository.getId(),
                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repository.getId(),
                                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("strongbox-metadata-plugin", metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals("org.carlspring.strongbox.maven", metadata.getGroupId(), "Incorrect groupId!");
        assertNull(versioning.getRelease(), "Incorrect latest release version!");

        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testMetadataMerge(@MavenSnapshotRepository("mm") Repository repository,
                                  @MavenArtifactWithClassifiers(repositoryId = "mm", id = "org.carlspring.strongbox:strongbox-metadata-merge", versions = "2.0-20180328.195810-1") Path snapshotArtifact)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                repository.getId(),
                                                "org/carlspring/strongbox/strongbox-metadata-merge");

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.0-SNAPSHOT");
        appendVersioning.addVersion("1.3-SNAPSHOT");

        SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMPED_SNAPSHOT_VERSION_FORMAT);
        Snapshot snapshot = new Snapshot();
        snapshot.setTimestamp(formatter.format(Calendar.getInstance().getTime()));
        snapshot.setBuildNumber(1);

        appendVersioning.setRelease(null);
        appendVersioning.setSnapshot(snapshot);
        appendVersioning.setLatest("1.3-SNAPSHOT");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        RepositoryPath snapshotPath = (RepositoryPath) snapshotArtifact.normalize();
        MavenArtifact mergeArtifact = MavenArtifactUtils.convertPathToArtifact(snapshotPath);
        artifactMetadataService.mergeMetadata(mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repository.getId(),
                                                                "org/carlspring/strongbox/strongbox-metadata-merge");

        assertNotNull(metadata);

        assertEquals("1.3-SNAPSHOT", metadata.getVersioning().getLatest(), "Incorrect latest release version!");
        assertEquals(3, metadata.getVersioning().getVersions().size(),
                     "Incorrect number of versions stored in metadata!");
    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @TestRepository(layout = MavenArtifactCoordinates.LAYOUT_NAME, policy = RepositoryPolicyEnum.SNAPSHOT)
    private static @interface MavenSnapshotRepository
    {
        @AliasFor(annotation = TestRepository.class, attribute = "repositoryId")
        String value();
    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(classifiers = { "javadoc",
                                       "sources",
                                       "source-release" })    
    private static @interface MavenArtifactWithClassifiers
    {

        @AliasFor(annotation = MavenTestArtifact.class)
        String id() default "";
        
        @AliasFor(annotation = MavenTestArtifact.class)
        String[] versions() default {};

        @AliasFor(annotation = MavenTestArtifact.class)
        String repositoryId() default "";
        
    }
    
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenArtifactWithClassifiers(versions = { "1.0-20180328.195810-1",
                                               "1.0-20180328.195810-2",
                                               "1.0-20180328.195810-3",
                                               "1.0-20180328.195810-4",
                                               "1.0-20180328.195810-5" })
    private static @interface MavenSnapshotArtifactsWithClassifiers
    {

        @AliasFor(annotation = MavenTestArtifact.class)
        String id() default "";

        @AliasFor(annotation = MavenTestArtifact.class)
        String repositoryId() default "";
    }

    
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(packaging = "maven-plugin")
    private static @interface MavenPluginArtifact
    {

        @AliasFor(annotation = MavenTestArtifact.class)
        String id() default "";
        
        @AliasFor(annotation = MavenTestArtifact.class)
        String[] versions() default {};
        
        @AliasFor(annotation = MavenTestArtifact.class)
        String repositoryId() default "";

    }
}
