package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 * @author stodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactMetadataServiceReleasesTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "amsr-releases";

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

    @Inject
    private ArtifactMetadataService artifactMetadataService;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testReleaseMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        createRelease("org.carlspring.strongbox.metadata.nested:foo:2.1:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:bar:3.1:jar");

        File strongboxMetadataDir = new File(REPOSITORY_BASEDIR,
                                             "org/carlspring/strongbox/metadata/strongbox-metadata");

        String ga = "org.carlspring.strongbox.metadata:strongbox-metadata";

        // Create released artifacts
        for (int i = 0; i <= 3; i++)
        {
            createRelease(ga + ":1." + i + ":jar");
        }

        assertFalse(new File(strongboxMetadataDir, "maven-metadata.xml").exists());

        // Testing scenario where 1.1 < 1.2 < 1.3 < 1.5 <  1.4
        // which might occur when 1.4 has been updated recently
        createRelease(ga + ":1.5:jar");

        MavenArtifact artifact = createRelease(ga + ":1.4:jar");

        changeCreationDate(artifact);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox/metadata");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_RELEASES,
                                                                "org/carlspring/strongbox/metadata/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(artifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(artifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        // TODO: Fix this as part of SB-333:
        //assertEquals(artifact.getVersion(), versioning.getRelease(), "Incorrect latest release version!");
        assertEquals("1.5", versioning.getRelease(), "Incorrect latest release version!");
        assertEquals(6, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");

        Metadata nestedMetadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                       REPOSITORY_RELEASES,
                                                                       "org/carlspring/strongbox/metadata/nested/foo");

        assertNotNull(nestedMetadata1);

        Metadata nestedMetadata2 = artifactMetadataService.getMetadata(STORAGE0,
                                                                       REPOSITORY_RELEASES,
                                                                       "org/carlspring/strongbox/metadata/nested/bar");

        assertNotNull(nestedMetadata2);
    }

    @Test
    public void testAddVersionToMetadata()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        createRelease("org.carlspring.strongbox:added:1.0:jar");
        createRelease("org.carlspring.strongbox:added:1.1:jar");
        createRelease("org.carlspring.strongbox:added:1.2:jar");
        createRelease("org.carlspring.strongbox:added:1.3:jar");

        String artifactPath = "org/carlspring/strongbox/added";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.3"), "Unexpected set of versions!");

        artifactMetadataService.addVersion(STORAGE0,
                                           REPOSITORY_RELEASES, artifactPath,
                                           "1.4",
                                           MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataAfter);
        assertTrue(MetadataHelper.containsVersion(metadataAfter, "1.4"), "Unexpected set of versions!");
        assertEquals("1.4", metadataAfter.getVersioning().getLatest(), "Unexpected set of versions!");
        assertEquals("1.4", metadataAfter.getVersioning().getRelease(), "Unexpected set of versions!");
    }

    @Test
    public void testDeleteVersionFromMetadata()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        createRelease("org.carlspring.strongbox:deleted:1.0:jar");
        createRelease("org.carlspring.strongbox:deleted:1.1:jar");
        createRelease("org.carlspring.strongbox:deleted:1.2:jar");
        createRelease("org.carlspring.strongbox:deleted:1.3:jar");

        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.3"), "Unexpected set of versions!");

        artifactMetadataService.removeVersion(STORAGE0,
                                              REPOSITORY_RELEASES,
                                              artifactPath,
                                              "1.3",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "1.3"), "Unexpected set of versions!");
    }

    @Test
    public void testReleasePluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create plugin artifact
        generatePluginArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                               "org.carlspring.strongbox.metadata.maven:strongbox-metadata-plugin",
                               "1.0");

        Artifact pluginArtifact = ArtifactUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox.metadata.maven:strongbox-metadata-plugin:1.0");

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/metadata/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_RELEASES,
                                                                "org/carlspring/strongbox/metadata/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(pluginArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(pluginArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        assertEquals(pluginArtifact.getVersion(), versioning.getRelease(), "Incorrect latest release version!");

        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
        // Create an artifact for metadata merging tests
        MavenArtifact mergeArtifact = createRelease("org.carlspring.strongbox.metadata:strongbox-metadata-merge:1.0:jar");

        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/metadata/strongbox-metadata-merge");

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
                                                                REPOSITORY_RELEASES,
                                                                "org/carlspring/strongbox/metadata/strongbox-metadata-merge");

        assertNotNull(metadata);

        assertEquals(mergeMetadata.getVersioning().getRelease(),
                     metadata.getVersioning().getRelease(),
                     "Incorrect latest release version!");
        assertEquals(3,
                     metadata.getVersioning().getVersions().size(),
                     "Incorrect number of versions stored in metadata!");
    }

    /**
     * Generate a released artifact.
     *
     * @param gavtc String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private MavenArtifact createRelease(String gavtc)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        MavenArtifact result = new MavenRepositoryArtifact(generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), gavtc));

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       REPOSITORY_RELEASES,
                                                                       ArtifactUtils.convertArtifactToPath(result));
        result.setPath(repositoryPath);
        
        return result;
    }

}
