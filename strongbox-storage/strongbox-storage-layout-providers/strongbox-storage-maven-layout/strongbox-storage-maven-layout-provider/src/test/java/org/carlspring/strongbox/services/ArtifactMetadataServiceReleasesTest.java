package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;
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
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String R1 = "amsr-releases1";
    private static final String R2 = "amsr-releases2";
    private static final String R3 = "amsr-releases3";
    private static final String R4 = "amsr-releases4";
    private static final String R5 = "amsr-releases5";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

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
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/strongbox-metadata";

        Path repositoryPath = repositoryPathResolver.resolve(repository);
        Path strongboxMetadataPath = repositoryPath.resolve(artifactPathStr);
        Path strongboxMetadataXmlPath = strongboxMetadataPath.resolve("strongbox-metadata.xml");

        assertTrue(Files.notExists(strongboxMetadataXmlPath));

        // Testing scenario where 1.0 < 1.1 < 1.2 < 1.3 < 1.5 < 1.4
        // which might occur when 1.4 has been updated recently
        RepositoryPath artifact14Path = (RepositoryPath) repositoryArtifact3.get(5).normalize();
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(artifact14Path);

        assertNotNull(artifact);
        changeCreationDate(artifact);

        artifactMetadataService.rebuildMetadata(STORAGE0, repositoryId, "org/carlspring/strongbox/metadata");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repositoryId,
                                                                artifactPathStr);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(artifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(artifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        // TODO: Fix this as part of SB-333:
        //assertEquals(artifact.getVersion(), versioning.getRelease(), "Incorrect latest release version!");
        assertEquals("1.5", versioning.getRelease(), "Incorrect latest release version!");
        assertEquals(6, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");

        Metadata nestedMetadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                       repositoryId,
                                                                       "org/carlspring/strongbox/metadata/nested/foo");

        assertNotNull(nestedMetadata1);

        Metadata nestedMetadata2 = artifactMetadataService.getMetadata(STORAGE0,
                                                                       repositoryId,
                                                                       "org/carlspring/strongbox/metadata/nested/bar");

        assertNotNull(nestedMetadata2);
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
        final String repositoryId = repository.getId();
        final String artifactPath = "org/carlspring/strongbox/added";

        artifactMetadataService.rebuildMetadata(STORAGE0, repositoryId, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, repositoryId, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.3"), "Unexpected set of versions!");

        artifactMetadataService.addVersion(STORAGE0,
                                           repositoryId,
                                           artifactPath,
                                           "1.4",
                                           MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, repositoryId, artifactPath);

        assertNotNull(metadataAfter);
        assertTrue(MetadataHelper.containsVersion(metadataAfter, "1.4"), "Unexpected set of versions!");
        assertEquals("1.4", metadataAfter.getVersioning().getLatest(), "Unexpected set of versions!");
        assertEquals("1.4", metadataAfter.getVersioning().getRelease(), "Unexpected set of versions!");
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
        final String repositoryId = repository.getId();
        final String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, repositoryId, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, repositoryId, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.3"), "Unexpected set of versions!");

        artifactMetadataService.removeVersion(STORAGE0,
                                              repositoryId,
                                              artifactPath,
                                              "1.3",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, repositoryId, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "1.3"), "Unexpected set of versions!");
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
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/maven/strongbox-metadata-plugin";

        Artifact pluginArtifact = MavenArtifactTestUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox.metadata.maven:strongbox-metadata-plugin:1.0");

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                repositoryId,
                                                artifactPathStr);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repositoryId,
                                                                artifactPathStr);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(pluginArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(pluginArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        assertEquals(pluginArtifact.getVersion(), versioning.getRelease(), "Incorrect latest release version!");

        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
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
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/strongbox/metadata/strongbox-metadata-merge";

        RepositoryPath repositoryPath = (RepositoryPath) artifactGroupPath.get(0).normalize();
        MavenArtifact mergeArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);

        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(STORAGE0,
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

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                repositoryId,
                                                                artifactPathStr);

        assertNotNull(metadata);

        assertEquals(mergeMetadata.getVersioning().getRelease(),
                     metadata.getVersioning().getRelease(),
                     "Incorrect latest release version!");
        assertEquals(3,
                     metadata.getVersioning().getVersions().size(),
                     "Incorrect number of versions stored in metadata!");
    }

}
