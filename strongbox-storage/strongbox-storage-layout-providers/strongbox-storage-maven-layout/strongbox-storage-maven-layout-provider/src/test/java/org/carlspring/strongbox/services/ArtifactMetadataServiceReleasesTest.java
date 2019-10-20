package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 * @author stodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactMetadataServiceReleasesTest
{
    private final Logger logger = LoggerFactory.getLogger(ArtifactMetadataServiceReleasesTest.class);

    private static final String R1 = "amsr-releases1";
    private static final String R2 = "amsr-releases2";
    private static final String R3 = "amsr-releases3";
    private static final String R4 = "amsr-releases4";
    private static final String R5 = "amsr-releases5";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testReleaseMetadataRebuild(@MavenRepository(repositoryId = R1)
                                           Repository repository,
                                           @MavenTestArtifact(repositoryId = R1,
                                                              id = "org.carlspring.strongbox.metadata.nested:foo",
                                                              versions = "2.1")
                                           List<Path> repositoryArtifact1,
                                           @MavenTestArtifact(repositoryId = R1,
                                                              id = "org.carlspring.strongbox.metadata.nested:bar",
                                                              versions = "3.1")
                                           List<Path> repositoryArtifact2,
                                           @MavenTestArtifact(repositoryId = R1,
                                                              id = "org.carlspring.strongbox.metadata:strongbox-metadata",
                                                              versions = { "1.0",
                                                                           "1.1",
                                                                           "1.2",
                                                                           "1.3",
                                                                           "1.5",
                                                                           "1.4" })
                                           List<Path> repositoryArtifact3)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/strongbox-metadata";

        Path repositoryPath = repositoryPathResolver.resolve(repository);
        Path strongboxMetadataPath = repositoryPath.resolve(artifactPathStr);
        Path strongboxMetadataXmlPath = strongboxMetadataPath.resolve("strongbox-metadata.xml");

        assertThat(Files.notExists(strongboxMetadataXmlPath)).isTrue();

        // Testing scenario where 1.0 < 1.1 < 1.2 < 1.3 < 1.5 < 1.4
        // which might occur when 1.4 has been updated recently
        RepositoryPath artifact14Path = (RepositoryPath) repositoryArtifact3.get(5).normalize();
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(artifact14Path);

        assertThat(artifact).isNotNull();
        changeCreationDate(artifact);

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, "org/carlspring/strongbox/metadata");

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPathStr);

        assertThat(metadata).isNotNull();

        Versioning versioning = metadata.getVersioning();

        assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo(artifact.getArtifactId());
        assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo(artifact.getGroupId());
        // TODO: Fix this as part of SB-333:
        //assertThat(versioning.getRelease()).as("Incorrect latest release version!").isEqualTo(artifact.getVersion());
        assertThat(versioning.getRelease()).as("Incorrect latest release version!").isEqualTo("1.5");
        assertThat(versioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(6);

        Metadata nestedMetadata1 = artifactMetadataService.getMetadata(storageId,
                                                                       repositoryId,
                                                                       "org/carlspring/strongbox/metadata/nested/foo");

        assertThat(nestedMetadata1).isNotNull();

        Metadata nestedMetadata2 = artifactMetadataService.getMetadata(storageId,
                                                                       repositoryId,
                                                                       "org/carlspring/strongbox/metadata/nested/bar");

        assertThat(nestedMetadata2).isNotNull();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testAddVersionToMetadata(@MavenRepository(repositoryId = R2)
                                         Repository repository,
                                         @MavenTestArtifact(repositoryId = R2,
                                                            id = "org.carlspring.strongbox:added",
                                                            versions = { "1.0",
                                                                         "1.1",
                                                                         "1.2",
                                                                         "1.3" })
                                         List<Path> artifactGroupPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPath = "org/carlspring/strongbox/added";

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataBefore).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataBefore, "1.3")).as("Unexpected set of versions!").isTrue();

        artifactMetadataService.addVersion(storageId,
                                           repositoryId,
                                           artifactPath,
                                           "1.4",
                                           MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataAfter).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataAfter, "1.4")).as("Unexpected set of versions!").isTrue();
        assertThat(metadataAfter.getVersioning().getLatest()).as("Unexpected set of versions!").isEqualTo("1.4");
        assertThat(metadataAfter.getVersioning().getRelease()).as("Unexpected set of versions!").isEqualTo("1.4");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeleteVersionFromMetadata(@MavenRepository(repositoryId = R3)
                                              Repository repository,
                                              @MavenTestArtifact(repositoryId = R3,
                                                                 id = "org.carlspring.strongbox:deleted",
                                                                 versions = { "1.0",
                                                                              "1.1",
                                                                              "1.2",
                                                                              "1.3" })
                                              List<Path> artifactGroupPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(storageId, repositoryId, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataBefore).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataBefore, "1.3")).as("Unexpected set of versions!").isTrue();

        artifactMetadataService.removeVersion(storageId,
                                              repositoryId,
                                              artifactPath,
                                              "1.3",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(storageId, repositoryId, artifactPath);

        assertThat(metadataAfter).isNotNull();
        assertThat(MetadataHelper.containsVersion(metadataAfter, "1.3")).as("Unexpected set of versions!").isFalse();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class  })
    @Test
    public void testReleasePluginMetadataRebuild(@MavenRepository(repositoryId = R4)
                                                 Repository repository,
                                                 @MavenTestArtifact(repositoryId = R4,
                                                                    id = "org.carlspring.strongbox.metadata.maven:strongbox-metadata-plugin",
                                                                    versions = { "1.0" },
                                                                    packaging = "maven-plugin")
                                                 List<Path> artifactGroupPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/maven/strongbox-metadata-plugin";

        Artifact pluginArtifact = MavenArtifactTestUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox.metadata.maven:strongbox-metadata-plugin:1.0");

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactPathStr);

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPathStr);

        assertThat(metadata).isNotNull();

        Versioning versioning = metadata.getVersioning();

        assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo(pluginArtifact.getArtifactId());
        assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo(pluginArtifact.getGroupId());
        assertThat(versioning.getRelease()).as("Incorrect latest release version!").isEqualTo(pluginArtifact.getVersion());

        assertThat(versioning.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
    }

    @ExtendWith({RepositoryManagementTestExecutionListener.class,
                 ArtifactManagementTestExecutionListener.class})
    @Test
    public void testMetadataMerge(@MavenRepository(repositoryId = R5)
                                  Repository repository,
                                  @MavenTestArtifact(repositoryId = R5,
                                                     id = "org.carlspring.strongbox.metadata:strongbox-metadata-merge",
                                                     versions = "1.0")
                                  List<Path> artifactGroupPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/strongbox-metadata-merge";

        RepositoryPath repositoryPath = (RepositoryPath) artifactGroupPath.get(0).normalize();
        MavenArtifact mergeArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);

        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactPathStr);

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.1");
        appendVersioning.addVersion("1.2");

        appendVersioning.setRelease("1.2");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        artifactMetadataService.mergeMetadata(mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                repositoryId,
                                                                artifactPathStr);

        assertThat(metadata).isNotNull();

        assertThat(mergeMetadata.getVersioning().getRelease())
                .as("Incorrect latest release version!")
                .isEqualTo(metadata.getVersioning().getRelease());

        assertThat(metadata.getVersioning().getVersions())
                .as("Incorrect number of versions stored in metadata!")
                .hasSize(3);
    }

    private void changeCreationDate(MavenArtifact artifact)
            throws IOException
    {
        Path directory = artifact.getPath().getParent();

        try (Stream<Path> pathStream = Files.walk(directory))
        {
            pathStream.filter(Files::isRegularFile).forEach(
                    filePath -> {
                        BasicFileAttributeView attributes = Files.getFileAttributeView(filePath,
                                                                                       BasicFileAttributeView.class);
                        FileTime time = FileTime.from(System.currentTimeMillis() + 60000L, TimeUnit.MILLISECONDS);
                        try
                        {
                            attributes.setTimes(time, time, time);
                        }
                        catch (IOException e)
                        {
                            logger.error("Failed to change creation date for [{}]",
                                         filePath, e);
                        }
                    });
        }
    }

}
