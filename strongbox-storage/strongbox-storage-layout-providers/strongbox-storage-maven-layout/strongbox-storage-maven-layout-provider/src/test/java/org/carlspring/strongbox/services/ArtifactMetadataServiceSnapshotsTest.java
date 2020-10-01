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
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author stodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactMetadataServiceSnapshotsTest
{
    
    private static final String REPOSITORY_SNAPSHOTS_1 = "amsst-snapshots-1";

    private static final String REPOSITORY_SNAPSHOTS_2 = "amsst-snapshots-2";

    private static final String REPOSITORY_SNAPSHOTS_3 = "amsst-snapshots-3";

    private static final String REPOSITORY_SNAPSHOTS_4 = "amsst-snapshots-4";

    private static final String REPOSITORY_SNAPSHOTS_5 = "amsst-snapshots-5";

    private static final String REPOSITORY_SNAPSHOTS_6 = "amsst-snapshots-6";

    private static final String REPOSITORY_SNAPSHOTS_7 = "amsst-snapshots-7";

    private static final String TIMESTAMPED_SNAPSHOT_VERSION_FORMAT = "yyyyMMdd.HHmmss";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testSnapshotMetadataRebuild(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_1) Repository repository,
                                            @MavenSnapshotArtifactsWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_1,
                                                                                   id = "org.carlspring.strongbox:strongbox-metadata") 
                                            List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        
        final String artifactPath = "org/carlspring/strongbox/strongbox-metadata";

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPath);

        assertThat(metadata).isNotNull();

        Versioning versioning = metadata.getVersioning();

        assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo("strongbox-metadata");
        assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo("org.carlspring.strongbox");

        assertThat(versioning.getVersions().isEmpty()).as("No versioning information could be found in the metadata!").isFalse();
        assertThat(versioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, 
                  ArtifactManagementTestExecutionListener.class })
    public void testDeleteVersionFromMetadata(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_2) Repository repository,
                                              @MavenSnapshotArtifactsWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_2,
                                                                                     id = "org.carlspring.strongbox:deleted") 
                                              List<Path> snapshotArtifacts)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        
        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataBefore).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataBefore, "1.0-SNAPSHOT")).as("Unexpected set of versions!").isTrue();

        artifactMetadataService.removeVersion(storageId,
                                              repositoryId,
                                              artifactPath,
                                              "1.0-SNAPSHOT",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataAfter).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataAfter, "1.0-SNAPSHOT")).as("Unexpected set of versions!").isFalse();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, 
                  ArtifactManagementTestExecutionListener.class })
    public void testAddTimestampedSnapshotVersionToMetadata(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_3)
                                                            Repository repository,
                                                            @MavenSnapshotArtifactsWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_3,
                                                                                                   id = "org.carlspring.strongbox:added") 
                                                            List<Path> snapshotArtifactPaths)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        
        String artifactPathStr = "org/carlspring/strongbox/added";
        Path artifactPath = Paths.get(artifactPathStr);

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPathStr);

        String metadataPath = artifactPath.resolve("1.0-SNAPSHOT").toString();
        Metadata metadataBefore = artifactMetadataService.getMetadata(storageId, repositoryId, metadataPath);
        for (Path snapshotArtifactPath : snapshotArtifactPaths)
        {
            RepositoryPath normalizedPath = (RepositoryPath) snapshotArtifactPath.normalize();
            MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(normalizedPath);
            assertThat(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, coordinates.getVersion())).isTrue();
        }

        SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMPED_SNAPSHOT_VERSION_FORMAT);
        String timestamp = formatter.format(Calendar.getInstance().getTime());
        String version = "1.0-" + timestamp + "-" + 6;

        MavenRepositoryArtifact addedArtifact = new MavenRepositoryArtifact(new Gav("org.carlspring.strongbox",
                                                                                    "added",
                                                                                    version));

        artifactMetadataService.addTimestampedSnapshotVersion(storageId,
                                                              repositoryId,
                                                              artifactPathStr,
                                                              addedArtifact.getVersion(),
                                                              null,
                                                              "jar");

        Metadata metadataAfter = artifactMetadataService.getMetadata(storageId, repositoryId, metadataPath);

        assertThat(metadataAfter).isNotNull();
        assertThat(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, addedArtifact.getVersion()))
                .as("Failed to add timestamped SNAPSHOT version to metadata!")
                .isTrue();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDeleteTimestampedSnapshotVersionFromMetadata(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_4)
                                                                 Repository repository,
                                                                 @MavenSnapshotArtifactsWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_4,
                                                                                                        id = "org.carlspring.strongbox:deleted")
                                                                 List<Path> snapshotArtifactPaths)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String artifactPathStr = "org/carlspring/strongbox/deleted";
        Path artifactPath = Paths.get(artifactPathStr);

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPathStr);

        String metadataPath = artifactPath.resolve("1.0-SNAPSHOT").toString();

        Metadata metadataBefore = artifactMetadataService.getMetadata(storageId, repositoryId, metadataPath);
        for (Path snapshotArtifactPath : snapshotArtifactPaths)
        {
            RepositoryPath normalizedPath = (RepositoryPath) snapshotArtifactPath.normalize();
            MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(normalizedPath);
            assertThat(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, coordinates.getVersion())).isTrue();
        }

        RepositoryPath normalizedPath = (RepositoryPath)snapshotArtifactPaths.iterator().next().normalize();
        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(normalizedPath);
        artifactMetadataService.removeTimestampedSnapshotVersion(storageId,
                                                                 repositoryId,
                                                                 artifactPathStr,
                                                                 coordinates.getVersion(),
                                                                 null);

        Metadata metadataAfter = artifactMetadataService.getMetadata(storageId, repositoryId, metadataPath);
        assertThat(metadataAfter).isNotNull();
        assertThat(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, coordinates.getVersion())).isFalse();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testSnapshotWithoutTimestampMetadataRebuild(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_5)
                                                            Repository repository,
                                                            @MavenArtifactWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_5,
                                                                                          id = "org.carlspring.strongbox.snapshots:metadata",
                                                                                          versions = "2.0-SNAPSHOT")
                                                            Path snapshotArtifactPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        String version = "2.0-SNAPSHOT";

        final String artifactPath = "org/carlspring/strongbox/snapshots/metadata";

        RepositoryPath normalizedPath = (RepositoryPath) snapshotArtifactPath.normalize();
        MavenArtifactCoordinates artifactCoordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(
                normalizedPath);

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadata).isNotNull();

        Versioning versioning = metadata.getVersioning();
        Versioning snapshotVersioning = snapshotMetadata.getVersioning();

        assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo(artifactCoordinates.getArtifactId());
        assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo(artifactCoordinates.getGroupId());

        assertThat(versioning.getVersions()).as("No versioning information could be found in the metadata!").isNotNull();
        assertThat(versioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
        assertThat(versioning.getLatest()).isEqualTo(version);
        assertThat(versioning.getLastUpdated()).as("Failed to set lastUpdated field!").isNotNull();

        assertThat(snapshotVersioning.getVersions())
                .as("No versioning information could be found in the metadata!")
                .isNotNull();
        assertThat(snapshotVersioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
        assertThat(snapshotVersioning.getLatest()).isEqualTo(version);
        assertThat(snapshotVersioning.getLastUpdated()).as("Failed to set lastUpdated field!").isNotNull();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testSnapshotPluginMetadataRebuild(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_6) Repository repository,
                                                  @MavenPluginArtifact(repositoryId = REPOSITORY_SNAPSHOTS_6,
                                                                       id = "org.carlspring.strongbox.maven:strongbox-metadata-plugin",
                                                                       versions = "1.1-20180328.195810-1")
                                                  Path pluginSnapshotPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        
        final String artifactPath = "org/carlspring/strongbox/maven/strongbox-metadata-plugin";

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPath);

        assertThat(metadata).isNotNull();

        Versioning versioning = metadata.getVersioning();

        assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo("strongbox-metadata-plugin");
        assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo("org.carlspring.strongbox.maven");
        assertThat(versioning.getRelease()).as("Incorrect latest release version!").isNull();

        assertThat(versioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testMetadataMerge(@MavenSnapshotRepository(REPOSITORY_SNAPSHOTS_7) Repository repository,
                                  @MavenArtifactWithClassifiers(repositoryId = REPOSITORY_SNAPSHOTS_7,
                                                                id = "org.carlspring.strongbox:strongbox-metadata-merge",
                                                                versions = "2.0-20180328.195810-1")
                                  Path snapshotArtifact)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        
        final String artifactPath = "org/carlspring/strongbox/strongbox-metadata-merge";

        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactPath);

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

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPath);

        assertThat(metadata).isNotNull();

        assertThat(metadata.getVersioning().getLatest()).as("Incorrect latest release version!").isEqualTo("1.3-SNAPSHOT");
        assertThat(metadata.getVersioning().getVersions())
                .as("Incorrect number of versions stored in metadata!")
                .hasSize(3);
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

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(classifiers = { "javadoc",
                                       "sources",
                                       "source-release" })    
    private @interface MavenArtifactWithClassifiers
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
    private @interface MavenSnapshotArtifactsWithClassifiers
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
    private @interface MavenPluginArtifact
    {

        @AliasFor(annotation = MavenTestArtifact.class)
        String id() default "";
        
        @AliasFor(annotation = MavenTestArtifact.class)
        String[] versions() default {};
        
        @AliasFor(annotation = MavenTestArtifact.class)
        String repositoryId() default "";

    }
}
