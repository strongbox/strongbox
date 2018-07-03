package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactMetadataServiceReleasesTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "amsr-releases";

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

    private static MavenArtifact artifact;

    @Inject
    private ArtifactMetadataService artifactMetadataService;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;


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
        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

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
        artifact = createRelease(ga + ":1.4:jar");

        changeCreationDate(artifact);

        createRelease("org.carlspring.strongbox.metadata.nested:foo:2.1:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:bar:3.1:jar");

        createRelease("org.carlspring.strongbox.metadata.nested:foo:2.1:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:foo:2.2:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:foo:2.3:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:bar:3.1:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:bar:3.2:jar");
        createRelease("org.carlspring.strongbox.metadata.nested:bar:3.3:jar");

        createRelease("org.carlspring.strongbox.metadata:nested:1.1:jar");
        createRelease("org.carlspring.strongbox.metadata:nested:1.2:jar");

        createRelease("org.carlspring.strongbox.metadata:utils:1.1:jar");
        createRelease("org.carlspring.strongbox.metadata:utils:1.2:jar");
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
        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox/metadata");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_RELEASES,
                                                                "org/carlspring/strongbox/metadata/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        // TODO: Fix this as part of SB-333:
        //Assert.assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());
        assertEquals("Incorrect latest release version!", "1.5", versioning.getRelease());
        assertEquals("Incorrect number of versions stored in metadata!", 6, versioning.getVersions().size());

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
        assertTrue("Unexpected set of versions!", MetadataHelper.containsVersion(metadataBefore, "1.3"));

        artifactMetadataService.addVersion(STORAGE0,
                                           REPOSITORY_RELEASES, artifactPath,
                                           "1.4",
                                           MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataAfter);
        assertTrue("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "1.4"));
        assertEquals("Unexpected set of versions!", "1.4", metadataAfter.getVersioning().getLatest());
        assertEquals("Unexpected set of versions!", "1.4", metadataAfter.getVersioning().getRelease());
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
        assertTrue("Unexpected set of versions!", MetadataHelper.containsVersion(metadataBefore, "1.3"));

        artifactMetadataService.removeVersion(STORAGE0,
                                              REPOSITORY_RELEASES,
                                              artifactPath,
                                              "1.3",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "1.3"));
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

        assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        assertEquals("Incorrect latest release version!", pluginArtifact.getVersion(), versioning.getRelease());

        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
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

        assertEquals("Incorrect latest release version!",
                     mergeMetadata.getVersioning().getRelease(),
                     metadata.getVersioning().getRelease());
        assertEquals("Incorrect number of versions stored in metadata!",
                     3,
                     metadata.getVersioning().getVersions().size());
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

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY_RELEASES, ArtifactUtils.convertArtifactToPath(result));
        result.setPath(repositoryPath);
        
        return result;
    }

}
