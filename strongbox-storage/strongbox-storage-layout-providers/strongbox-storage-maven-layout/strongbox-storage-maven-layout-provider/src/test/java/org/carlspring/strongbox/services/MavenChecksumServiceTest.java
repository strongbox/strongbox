package org.carlspring.strongbox.services;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum.SNAPSHOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kate Novik.
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class MavenChecksumServiceTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "mcs-releases";

    private static final String REPOSITORY_SNAPSHOTS = "mcs-snapshots";

    private static final String A1 = "org.carlspring.strongbox.checksum.maven:strongbox-checksum";
    
    private static final String A3 = "org/carlspring/strongbox/checksum/maven/checksum-rewrite/1.0/checksum-rewrite-1.0.jar";
    
    private static final String S2 = "org/carlspring/strongbox/checksum/maven/strongbox-checksum/2.0-SNAPSHOT/strongbox-checksum-2.0-20180320.011625-1.jar";
    
    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ChecksumService checksumService;

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testGenerateMavenChecksumForReleaseArtifact(@TestRepository(repositoryId = REPOSITORY_RELEASES, layout = LAYOUT_NAME) Repository repository,
                                                            @TestArtifact(repositoryId = REPOSITORY_RELEASES, id = A1, versions = { "1.0", "2.0"}, generator = MavenArtifactGenerator.class) List<Path> artifactGroupPath)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Path artifact1 = artifactGroupPath.get(0);
        Path artifact2 = artifactGroupPath.get(1);
                
        // Remove these for the sake of the test:
        Path artifact1Md5 = artifact1.resolveSibling("strongbox-checksum-1.0.jar.md5");
        Path artifact1Sha1 = artifact1.resolveSibling("strongbox-checksum-1.0.jar.sha1");
        Path artifact1PomMd5 = artifact1.resolveSibling("strongbox-checksum-1.0.pom.md5");
        Path artifact1PomSha1 = artifact1.resolveSibling("strongbox-checksum-1.0.pom.sha1");
        
        Path artifact2Md5 = artifact2.resolveSibling("strongbox-checksum-2.0.jar.md5");
        Path artifact2Sha1 = artifact2.resolveSibling("strongbox-checksum-2.0.jar.sha1");
        Path artifact2PomMd5 = artifact2.resolveSibling("strongbox-checksum-2.0.pom.md5");
        Path artifact2PomSha1 = artifact2.resolveSibling("strongbox-checksum-2.0.pom.sha1");

        Path artifactMetadataMd5 = artifact1.getParent().getParent().resolve("maven-metadata.xml.md5");
        Path artifactMetadataSha1 = artifact1.getParent().getParent().resolve("maven-metadata.xml.sha1");
        
        Files.delete(artifact1Md5);
        Files.delete(artifact1Sha1);

        assertFalse(Files.exists(artifact1Md5), "The checksum file for artifact exist!");
        assertFalse(Files.exists(artifact1Sha1), "The checksum file for artifact exist!");
        
        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/checksum/maven/strongbox-checksum");

        checksumService.regenerateChecksum(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/strongbox-checksum",
                                           false);

        assertTrue(Files.exists(artifact1Sha1),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(Files.size(artifact1Sha1) > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(Files.exists(artifact1Md5),
                "The checksum file for artifact doesn't exist!");
        assertTrue(Files.size(artifact1Md5) > 0,
                "The checksum file for artifact is empty!");

        assertTrue(Files.exists(artifact1PomMd5),
                "The checksum file for pom file doesn't exist!");
        assertTrue(Files.size(artifact1PomMd5) > 0,
                "The checksum file for pom file is empty!");
        assertTrue(Files.exists(artifact1PomSha1),
                "The checksum file for pom file doesn't exist!");
        assertTrue(Files.size(artifact1PomSha1) > 0,
                "The checksum file for pom file is empty!");
        
        assertTrue(Files.exists(artifact2Sha1),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(Files.size(artifact2Sha1) > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(Files.exists(artifact2Md5),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(Files.size(artifact2Md5) > 0,
                   "The checksum file for artifact is empty!");

        assertTrue(Files.exists(artifact2PomMd5),
                   "The checksum file for pom file doesn't exist!");
        assertTrue(Files.size(artifact2PomSha1) > 0,
                   "The checksum file for pom file is empty!");
     
        assertTrue(Files.exists(artifactMetadataMd5),
                   "The checksum file for metadata doesn't exist!");
        assertTrue(Files.size(artifactMetadataMd5) > 0,
                   "The checksum file for metadata is empty!");
        assertTrue(Files.exists(artifactMetadataSha1),
                "The checksum file for metadata doesn't exist!");
        assertTrue(Files.size(artifactMetadataSha1) > 0,
                   "The checksum file for metadata is empty!");
     }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testGenerateMavenChecksumForSnapshotArtifact(@TestRepository(repositoryId = REPOSITORY_SNAPSHOTS, policy = SNAPSHOT, layout = LAYOUT_NAME) Repository repository,
                                                             @TestArtifact(repositoryId = REPOSITORY_SNAPSHOTS, resource = S2, generator = MavenArtifactGenerator.class) Path artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Path artifact1Md5 = artifact.resolveSibling(artifact.getFileName() + ".md5");
        Path artifact1Sha1 = artifact.resolveSibling(artifact.getFileName() + ".sha1");
        Path artifact1PomMd5 = artifact.resolveSibling(artifact.getFileName().toString().replace(".jar", ".pom") + ".md5");
        Path artifact1PomSha1 = artifact.resolveSibling(artifact.getFileName().toString().replace(".jar", ".pom") + ".sha1");

        Path artifactMetadataMd5 = artifact.getParent().getParent().resolve("maven-metadata.xml.md5");
        Path artifactMetadataSha1 = artifact.getParent().getParent().resolve("maven-metadata.xml.sha1");
        
        Files.delete(artifact1Md5);
        Files.delete(artifact1Sha1);
        Files.delete(artifact1PomMd5);
        Files.delete(artifact1PomSha1);
        

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/checksum");

        assertFalse(Files.exists(artifact1Md5),
                "The checksum file for artifact exist!");
        assertFalse(Files.exists(artifact1Sha1),
                    "The checksum file for artifact exist!");

        checksumService.regenerateChecksum(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/checksum", false);
       
        assertTrue(Files.exists(artifact1Sha1),
                   "The checksum file for the artifact doesn't exist!");
        assertTrue(Files.size(artifact1Sha1) > 0,
                   "The checksum file for the artifact is empty!");
        assertTrue(Files.exists(artifact1Md5),
                   "The checksum file for the artifact doesn't exist!");
        assertTrue(Files.size(artifact1Md5) > 0,
                   "The checksum file for the artifact is empty!");

        assertTrue(Files.exists(artifact1PomSha1),
                   "The checksum file for the pom file doesn't exist!");
        assertTrue(Files.size(artifact1PomSha1) > 0,
                   "The checksum file for the pom file is empty!");
        assertTrue(Files.exists(artifact1PomMd5),
                   "The checksum file for the pom file doesn't exist!");
        assertTrue(Files.size(artifact1PomMd5) > 0,
                   "The checksum file for the pom file is empty!");
        
        assertTrue(Files.exists(artifactMetadataMd5),
                   "The checksum file for metadata doesn't exist!");
        assertTrue(Files.size(artifactMetadataMd5) > 0,
                   "The checksum file for metadata is empty!");
        assertTrue(Files.exists(artifactMetadataSha1),
                   "The checksum file for metadata doesn't exist!");
        assertTrue(Files.size(artifactMetadataSha1) > 0,
                   "The checksum file for metadata is empty!");
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testRewriteMavenChecksum(@TestRepository(repositoryId = REPOSITORY_RELEASES, layout = LAYOUT_NAME) Repository repository,
                                         @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = A3, generator = MavenArtifactGenerator.class) Path artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox/checksum");

        Path md5File = artifact.resolveSibling(artifact.getFileName() + ".md5");
        Path sha1File = artifact.resolveSibling(artifact.getFileName() + ".sha1");
        
        Path pomMd5 = artifact.resolveSibling(artifact.getFileName().toString().replace(".jar", ".pom") + ".md5");
        Path pomSha1 = artifact.resolveSibling(artifact.getFileName().toString().replace(".jar", ".pom") + ".sha1");
        
        Path artifactMetadataMd5 = artifact.getParent().getParent().resolve("maven-metadata.xml.md5");
        Path artifactMetadataSha1 = artifact.getParent().getParent().resolve("maven-metadata.xml.sha1");

        assertTrue(Files.exists(md5File),
                   "The checksum file for artifact doesn't exist!");
        assertTrue(Files.exists(sha1File),
                   "The checksum file for pom file doesn't exist!");
        assertTrue(Files.exists(artifactMetadataSha1),
                   "The checksum file for metadata doesn't exist!");
        assertTrue(Files.exists(artifactMetadataMd5),
                "The checksum file for metadata doesn't exist!");

        try (
                    OutputStream os1 = Files.newOutputStream(md5File, CREATE_NEW, TRUNCATE_EXISTING);
                    OutputStream os2 = Files.newOutputStream(sha1File, CREATE_NEW, TRUNCATE_EXISTING);
                    OutputStream os3 = Files.newOutputStream(pomMd5, CREATE_NEW, TRUNCATE_EXISTING);
                    OutputStream os4 = Files.newOutputStream(pomSha1, CREATE_NEW, TRUNCATE_EXISTING);
                    OutputStream os5 = Files.newOutputStream(artifactMetadataMd5, CREATE_NEW, TRUNCATE_EXISTING);
                    OutputStream os6 = Files.newOutputStream(artifactMetadataSha1, CREATE_NEW, TRUNCATE_EXISTING);
        )
        {
            os1.write("".getBytes());
            os2.write("".getBytes());
            os3.write("".getBytes());
            os4.write("".getBytes());
            os5.write("".getBytes());
            os6.write("".getBytes());
        }

        assertEquals(0, Files.size(md5File), "The checksum file for artifact isn't empty!");
        assertEquals(0, Files.size(pomSha1),
                     "The checksum file for pom file isn't empty!");
        assertEquals(0, Files.size(artifactMetadataMd5),
                     "The checksum file for metadata isn't empty!");

        checksumService.regenerateChecksum(STORAGE0,
                                           REPOSITORY_RELEASES,
                                           "org/carlspring/strongbox/checksum/maven/checksum-rewrite",
                                           true);

        assertTrue(Files.size(md5File) > 0, "The checksum file for artifact is empty!");
        assertTrue(Files.size(pomSha1) > 0,
                   "The checksum file for pom file is empty!");
        assertTrue(Files.size(artifactMetadataMd5) > 0,
                   "The checksum file for metadata is empty!");
    }

}
