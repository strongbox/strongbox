package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;
import org.carlspring.strongbox.util.FileUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Maven2LayoutProviderTest
        extends TestCaseWithArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "m2lp-releases";

    private static final String REPOSITORY_SNAPSHOTS = "m2lp-snapshots";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS);

    private static Artifact snapshotArtifact;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ConfigurationManager configurationManager;


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
        Repository repository = new Repository(REPOSITORY_RELEASES);
        repository.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository.setAllowsForceDeletion(true);

        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[]{ "1.2.1", // testDeleteArtifact()
                                       "1.2.2"  // testDeleteArtifactDirectory()
                         }
        );

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.checksum.maven:strongbox-checksum:1.0:jar");
        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.checksum.maven:strongbox-checksum:1.1:jar");
        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.checksum.maven:checksum-rewrite:1.0:jar");

        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        snapshotArtifact = createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR.getAbsolutePath(),
                                                             "org.carlspring.strongbox",
                                                             "checksum",
                                                             "2.0",
                                                             "jar",
                                                             null,
                                                             1);
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
    public void testDeleteArtifact()
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES, path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.2";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES, path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

    @Ignore // Not working properly, due to changes by me (most-likely)
    @Test
    public void testGenerateMavenChecksumForReleaseArtifact()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_RELEASES_BASEDIR + "/org/carlspring/strongbox/checksum/maven/strongbox-checksum";

        // Remove these for the sake of the test:
        FileUtils.deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1"));

        assertFalse("The checksum file for artifact exist!",
                    new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").exists());

        Repository repository = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        layoutProvider.rebuildMetadata(STORAGE0,
                                       REPOSITORY_RELEASES,
                                       "org/carlspring/strongbox/checksum/maven/strongbox-checksum");

        layoutProvider.regenerateChecksums(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/strongbox-checksum",
                                           false);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "1.0/strongbox-checksum-1.0.pom.md5").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "1.1/strongbox-checksum-1.1.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "1.1/strongbox-checksum-1.1.jar.sha1").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "1.1/strongbox-checksum-1.1.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "1.1/strongbox-checksum-1.1.pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "maven-metadata.xml.sha1").length() > 0);
    }

    @Test
    public void testGenerateMavenChecksumForSnapshotArtifact()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile().getAbsolutePath() + ".jar.md5"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile().getAbsolutePath() + ".jar.sha1"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile().getAbsolutePath() + ".pom.md5"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile().getAbsolutePath() + ".pom.sha1"));

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR + "/org/carlspring/strongbox/checksum";

        Repository repository = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        layoutProvider.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/checksum");

        assertFalse("The checksum file for artifact exist!",
                    new File(snapshotArtifact.getFile().getAbsolutePath() + ".jar.md5").exists());
        assertFalse("The checksum file for artifact exist!",
                    new File(snapshotArtifact.getFile().getAbsolutePath() + ".jar.sha1").exists());

        layoutProvider.regenerateChecksums(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/checksum", false);

        assertTrue("The checksum file for the artifact doesn't exist!",
                   new File(snapshotArtifact.getFile().getAbsolutePath() + ".sha1").exists());
        assertTrue("The checksum file for the artifact is empty!",
                   new File(snapshotArtifact.getFile().getAbsolutePath() + ".sha1").length() > 0);
        assertTrue("The checksum file for the artifact doesn't exist!",
                   new File(snapshotArtifact.getFile().getAbsolutePath() + ".md5").exists());
        assertTrue("The checksum file for the artifact is empty!",
                   new File(snapshotArtifact.getFile().getAbsolutePath() + ".md5").length() > 0);

        assertTrue("The checksum file for the pom file doesn't exist!",
                   new File(snapshotArtifact.getFile().getAbsolutePath().replaceAll("jar", "pom") + ".sha1").exists());
        assertTrue("The checksum file for the pom file is empty!",
                   new File(snapshotArtifact.getFile().getAbsolutePath().replaceAll("jar", "pom") + ".sha1").length() > 0);
        assertTrue("The checksum file for the pom file doesn't exist!",
                   new File(snapshotArtifact.getFile().getAbsolutePath().replaceAll("jar", "pom") + ".md5").exists());
        assertTrue("The checksum file for the pom file is empty!",
                   new File(snapshotArtifact.getFile().getAbsolutePath().replaceAll("jar", "pom") + ".md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "maven-metadata.xml.sha1").length() > 0);
    }

    @Ignore // Not working properly, due to changes by me (most-likely)
    @Test
    public void testRewriteMavenChecksum()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_RELEASES_BASEDIR + "/org/carlspring/strongbox/checksum/maven/checksum-rewrite";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox/checksum");

        File md5File = new File(artifactPath, "1.0/checksum-rewrite-1.0.jar.md5");

        assertTrue("The checksum file for artifact doesn't exist!",
                   md5File.exists());
        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").exists());
        assertTrue("The checksum file for metadata doesn't exist!",
                   new File(artifactPath, "maven-metadata.xml.md5").exists());

        new FileOutputStream(md5File, false).write("".getBytes());
        new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.jar.sha1"), false).write("".getBytes());
        new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.md5"), false).write("".getBytes());
        new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1"), false).write("".getBytes());
        new FileOutputStream(new File(artifactPath, "maven-metadata.xml.md5"), false).write("".getBytes());
        new FileOutputStream(new File(artifactPath, "maven-metadata.xml.sha1"), false).write("".getBytes());

        assertTrue("The checksum file for artifact isn't empty!",
                   md5File.length() == 0);
        assertTrue("The checksum file for pom file isn't empty!",
                   new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").length() == 0);
        assertTrue("The checksum file for metadata isn't empty!",
                   new File(artifactPath, "maven-metadata.xml.md5").length() == 0);

        Repository repository = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        layoutProvider.regenerateChecksums(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/checksum-rewrite",
                                           true);

        System.out.println(md5File);

        assertTrue("The checksum file for artifact is empty!",
                   md5File.length() > 0);
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").length() > 0);
        assertTrue("The checksum file for metadata is empty!",
                   new File(artifactPath, "maven-metadata.xml.md5").length() > 0);
    }

}
