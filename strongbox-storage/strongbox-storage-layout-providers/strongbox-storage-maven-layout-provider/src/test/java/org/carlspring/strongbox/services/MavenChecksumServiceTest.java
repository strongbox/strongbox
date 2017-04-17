package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MavenChecksumServiceTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "mcs-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mcs-snapshots";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_RELEASES);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/" + STORAGE0 + "/" +
                                                                      REPOSITORY_SNAPSHOTS);

    private static Artifact snapshotArtifact;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ChecksumService checksumService;

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
        repository.setStorage(configurationManager.getConfiguration()
                                                  .getStorage(STORAGE0));
        repository.setAllowsForceDeletion(true);

        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

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
    public void testGenerateMavenChecksumForReleaseArtifact()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        String artifactPath =
                REPOSITORY_RELEASES_BASEDIR + "/org/carlspring/strongbox/checksum/maven/strongbox-checksum";

        // Remove these for the sake of the test:
        FileUtils.deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1"));

        assertFalse("The checksum file for artifact exist!",
                    new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").exists());

        getLayoutProvider(REPOSITORY_RELEASES).rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                               "org/carlspring/strongbox/checksum/maven/strongbox-checksum");

        checksumService.regenerateChecksum(STORAGE0,
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
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile()
                                                          .getAbsolutePath() + ".jar.md5"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile()
                                                          .getAbsolutePath() + ".jar.sha1"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile()
                                                          .getAbsolutePath() + ".pom.md5"));
        FileUtils.deleteIfExists(new File(snapshotArtifact.getFile()
                                                          .getAbsolutePath() + ".pom.sha1"));

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR + "/org/carlspring/strongbox/checksum";

        getLayoutProvider(REPOSITORY_SNAPSHOTS).rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/checksum");

        assertFalse("The checksum file for artifact exist!",
                    new File(snapshotArtifact.getFile()
                                             .getAbsolutePath() + ".jar.md5").exists());
        assertFalse("The checksum file for artifact exist!",
                    new File(snapshotArtifact.getFile()
                                             .getAbsolutePath() + ".jar.sha1").exists());

        checksumService.regenerateChecksum(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/checksum", false);

        assertTrue("The checksum file for the artifact doesn't exist!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath() + ".sha1").exists());
        assertTrue("The checksum file for the artifact is empty!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath() + ".sha1").length() > 0);
        assertTrue("The checksum file for the artifact doesn't exist!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath() + ".md5").exists());
        assertTrue("The checksum file for the artifact is empty!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath() + ".md5").length() > 0);

        assertTrue("The checksum file for the pom file doesn't exist!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath()
                                            .replaceAll("jar", "pom") + ".sha1").exists());
        assertTrue("The checksum file for the pom file is empty!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath()
                                            .replaceAll("jar", "pom") + ".sha1").length() > 0);
        assertTrue("The checksum file for the pom file doesn't exist!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath()
                                            .replaceAll("jar", "pom") + ".md5").exists());
        assertTrue("The checksum file for the pom file is empty!",
                   new File(snapshotArtifact.getFile()
                                            .getAbsolutePath()
                                            .replaceAll("jar", "pom") + ".md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "maven-metadata.xml.sha1").length() > 0);
    }

    @Test
    public void testRewriteMavenChecksum()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_RELEASES_BASEDIR + "/org/carlspring/strongbox/checksum/maven/checksum-rewrite";

        getLayoutProvider(REPOSITORY_RELEASES).rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                               "org/carlspring/strongbox/checksum");

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

        checksumService.regenerateChecksum(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/checksum-rewrite",
                                           true);

        System.out.println(md5File);

        assertTrue("The checksum file for artifact is empty!", md5File.length() > 0);
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").length() > 0);
        assertTrue("The checksum file for metadata is empty!",
                   new File(artifactPath, "maven-metadata.xml.md5").length() > 0);
    }

    private LayoutProvider getLayoutProvider(String repositoryId)
    {
        Repository repository = configurationManager.getRepository(STORAGE0, repositoryId);

        return layoutProviderRegistry.getProvider(repository.getLayout());
    }
}
