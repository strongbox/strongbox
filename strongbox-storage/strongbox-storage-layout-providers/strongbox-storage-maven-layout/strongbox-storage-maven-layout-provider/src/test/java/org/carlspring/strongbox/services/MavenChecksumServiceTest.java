package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
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

    private static MavenArtifact snapshotArtifact;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ChecksumService checksumService;


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

        MutableRepository repository = new MutableRepository(REPOSITORY_RELEASES);
        repository.setStorage(getStorage(STORAGE0));
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

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS, Maven2LayoutProvider.ALIAS));

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
        deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5"));
        deleteIfExists(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1"));

        assertFalse(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").exists(),
                    "The checksum file for artifact exist!");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/checksum/maven/strongbox-checksum");

        checksumService.regenerateChecksum(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/strongbox-checksum",
                                           false);

        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1").exists(),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.sha1").length() > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").exists(),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.jar.md5").length() > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.pom.sha1").exists(),
                   "The checksum file for pom file doesn't exist!");
        assertTrue(new File(artifactPath, "1.0/strongbox-checksum-1.0.pom.md5").length() > 0,
                   "The checksum file for pom file is empty!");

        assertTrue(new File(artifactPath, "1.1/strongbox-checksum-1.1.jar.sha1").exists(),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(new File(artifactPath, "1.1/strongbox-checksum-1.1.jar.sha1").length() > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(new File(artifactPath, "1.1/strongbox-checksum-1.1.pom.sha1").exists(),
                   "The checksum file for pom file doesn't exist!");
        assertTrue(new File(artifactPath, "1.1/strongbox-checksum-1.1.pom.md5").length() > 0,
                   "The checksum file for pom file is empty!");

        assertTrue(new File(artifactPath, "maven-metadata.xml.md5").exists(),
                   "The checksum file for metadata file doesn't exist!");
        assertTrue(new File(artifactPath, "maven-metadata.xml.sha1").length() > 0,
                   "The checksum file for metadata file is empty!");
    }

    @Test
    public void testGenerateMavenChecksumForSnapshotArtifact()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        deleteIfExists(new File(snapshotArtifact.getPath().toString() + ".jar.md5"));
        deleteIfExists(new File(snapshotArtifact.getPath().toString() + ".jar.sha1"));
        deleteIfExists(new File(snapshotArtifact.getPath().toString() + ".pom.md5"));
        deleteIfExists(new File(snapshotArtifact.getPath().toString() + ".pom.sha1"));

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR + "/org/carlspring/strongbox/checksum";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/checksum");

        assertFalse(new File(snapshotArtifact.getPath().toString() + ".jar.md5").exists(),
                    "The checksum file for artifact exist!");
        assertFalse(new File(snapshotArtifact.getPath().toString() + ".jar.sha1").exists(),
                    "The checksum file for artifact exist!");

        checksumService.regenerateChecksum(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/checksum", false);

        assertTrue(new File(snapshotArtifact.getPath().toString() + ".sha1").exists(),
                   "The checksum file for the artifact doesn't exist!");
        assertTrue(new File(snapshotArtifact.getPath().toString() + ".sha1").length() > 0,
                   "The checksum file for the artifact is empty!");
        assertTrue(new File(snapshotArtifact.getPath().toString() + ".md5").exists(),
                   "The checksum file for the artifact doesn't exist!");
        assertTrue(new File(snapshotArtifact.getPath().toString() + ".md5").length() > 0,
                   "The checksum file for the artifact is empty!");

        assertTrue(new File(snapshotArtifact.getPath().toString()
                                            .replaceAll("jar", "pom") + ".sha1").exists(),
                   "The checksum file for the pom file doesn't exist!");
        assertTrue(new File(snapshotArtifact.getPath().toString()
                                            .replaceAll("jar", "pom") + ".sha1").length() > 0,
                   "The checksum file for the pom file is empty!");
        assertTrue(new File(snapshotArtifact.getPath().toString()
                                            .replaceAll("jar", "pom") + ".md5").exists(),
                   "The checksum file for the pom file doesn't exist!");
        assertTrue(new File(snapshotArtifact.getPath().toString()
                                            .replaceAll("jar", "pom") + ".md5").length() > 0,
                   "The checksum file for the pom file is empty!");

        assertTrue(new File(artifactPath, "maven-metadata.xml.md5").exists(),
                   "The checksum file for metadata file doesn't exist!");
        assertTrue(new File(artifactPath, "maven-metadata.xml.sha1").length() > 0,
                   "The checksum file for metadata file is empty!");
    }

    @Test
    public void testRewriteMavenChecksum()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_RELEASES_BASEDIR + "/org/carlspring/strongbox/checksum/maven/checksum-rewrite";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                               "org/carlspring/strongbox/checksum");

        File md5File = new File(artifactPath, "1.0/checksum-rewrite-1.0.jar.md5");

        assertTrue(md5File.exists(),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").exists(),
                   "The checksum file for pom file doesn't exist!");
        assertTrue(new File(artifactPath, "maven-metadata.xml.md5").exists(),
                   "The checksum file for metadata doesn't exist!");

        try (
                    OutputStream os1 = new FileOutputStream(md5File, false);
                    OutputStream os2 = new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.jar.sha1"),
                                                            false);
                    OutputStream os3 = new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.md5"),
                                                            false);
                    OutputStream os4 = new FileOutputStream(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1"),
                                                            false);
                    OutputStream os5 = new FileOutputStream(new File(artifactPath, "maven-metadata.xml.md5"), false);
                    OutputStream os6 = new FileOutputStream(new File(artifactPath, "maven-metadata.xml.sha1"), false);
        )
        {
            os1.write("".getBytes());
            os2.write("".getBytes());
            os3.write("".getBytes());
            os4.write("".getBytes());
            os5.write("".getBytes());
            os6.write("".getBytes());
        }

        assertEquals(0, md5File.length(), "The checksum file for artifact isn't empty!");
        assertEquals(0, new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").length(),
                     "The checksum file for pom file isn't empty!");
        assertEquals(0, new File(artifactPath, "maven-metadata.xml.md5").length(),
                     "The checksum file for metadata isn't empty!");

        checksumService.regenerateChecksum(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/checksum-rewrite",
                                           true);

        System.out.println(md5File);

        assertTrue(md5File.length() > 0, "The checksum file for artifact is empty!");
        assertTrue(new File(artifactPath, "1.0/checksum-rewrite-1.0.pom.sha1").length() > 0,
                   "The checksum file for pom file is empty!");
        assertTrue(new File(artifactPath, "maven-metadata.xml.md5").length() > 0,
                   "The checksum file for metadata is empty!");
    }

}
