package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum.SNAPSHOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class MavenChecksumServiceTest
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
    @ExtendWith({RepositoryManagementTestExecutionListener.class, 
                 ArtifactManagementTestExecutionListener.class})
    public void testGenerateMavenChecksumForReleaseArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                                            Repository repository,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                               id = A1,
                                                                               versions = { "1.0",
                                                                                            "2.0" })
                                                            List<Path> artifactGroupPath)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        Path artifact1 = artifactGroupPath.get(0);

        // JAR MD5 file.
        String fileName1 = artifact1.getFileName().toString();
        String checksumFileName1 = fileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact1Md5 = artifact1.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1Md5);
        assertThat(Files.exists(artifact1Md5)).as("The checksum file for artifact exist!").isFalse();

        // JAR SHA1 file.
        checksumFileName1 = fileName1 + ".sha1";
        Path artifact1Sha1 = artifact1.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1Sha1);
        assertThat(Files.exists(artifact1Sha1)).as("The checksum file for artifact exist!").isFalse();

        // POM MD5 file.
        String pomFileName1 = fileName1.replace("jar", "pom");
        checksumFileName1 = pomFileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact1PomMd5 = artifact1.resolveSibling(checksumFileName1);

        // POM SHA1 file.
        checksumFileName1 = pomFileName1 + ".sha1";
        Path artifact1PomSha1 = artifact1.resolveSibling(checksumFileName1);

        // Metadata XML MD5 file.
        String metadataXmlFileName = "maven-metadata.xml";
        checksumFileName1 = metadataXmlFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactMetadataMd5 = artifact1.getParent().getParent().resolve(checksumFileName1);

        // Metadata XML SHA1 file.
        checksumFileName1 = metadataXmlFileName + ".sha1";
        Path artifactMetadataSha1 = artifact1.getParent().getParent().resolve(checksumFileName1);


        Path artifact2 = artifactGroupPath.get(1);

        // JAR MD5 file.
        String fileName2 = artifact2.getFileName().toString();
        String checksumFileName2 = fileName2 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact2Md5 = artifact2.resolveSibling(checksumFileName2);
        deleteIfExists(artifact2Md5);
        assertThat(Files.exists(artifact2Md5)).as("The checksum file for artifact exist!").isFalse();

        // JAR SHA1 file.
        checksumFileName2 = fileName2 + ".sha1";
        Path artifact2Sha1 = artifact2.resolveSibling(checksumFileName2);
        deleteIfExists(artifact1Sha1);
        assertThat(Files.exists(artifact1Sha1)).as("The checksum file for artifact exist!").isFalse();

        // POM MD5 file.
        String pomFileName2 = fileName2.replace("jar", "pom");
        checksumFileName2 = pomFileName2 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact2PomMd5 = artifact2.resolveSibling(checksumFileName2);

        // POM SHA1 file.
        checksumFileName2 = pomFileName2 + ".sha1";
        Path artifact2PomSha1 = artifact2.resolveSibling(checksumFileName2);

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                "org/carlspring/strongbox/checksum/maven/strongbox-checksum");

        checksumService.regenerateChecksum(storageId,
                                           repositoryId,
                                           "org/carlspring/strongbox/checksum/maven/strongbox-checksum",
                                           false);

        assertThat(Files.exists(artifact1Sha1))
                .as("The checksum file for artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1Sha1) > 0)
                .as("The checksum file for artifact is empty!")
                .isTrue();

        assertThat(Files.exists(artifact1Md5))
                .as("The checksum file for artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1Md5) > 0)
                .as("The checksum file for artifact is empty!")
                .isTrue();

        assertThat(Files.exists(artifact1PomMd5))
                .as("The checksum file for pom file doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1PomMd5) > 0)
                .as("The checksum file for pom file is empty!")
                .isTrue();
        assertThat(Files.exists(artifact1PomSha1))
                .as("The checksum file for pom file doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1PomSha1) > 0)
                .as("The checksum file for pom file is empty!")
                .isTrue();
        
        assertThat(Files.exists(artifact2Sha1))
                .as("The checksum file for artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact2Sha1) > 0)
                .as("The checksum file for artifact is empty!")
                .isTrue();

        assertThat(Files.exists(artifact2Md5))
                .as("The checksum file for artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact2Md5) > 0)
                .as("The checksum file for artifact is empty!")
                .isTrue();

        assertThat(Files.exists(artifact2PomMd5))
                .as("The checksum file for pom file doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact2PomSha1) > 0)
                .as("The checksum file for pom file is empty!")
                .isTrue();
     
        assertThat(Files.exists(artifactMetadataMd5))
                .as("The checksum file for metadata doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifactMetadataMd5) > 0)
                .as("The checksum file for metadata is empty!")
                .isTrue();
        assertThat(Files.exists(artifactMetadataSha1))
                .as("The checksum file for metadata doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifactMetadataSha1) > 0)
                .as("The checksum file for metadata is empty!")
                .isTrue();
     }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, 
                 ArtifactManagementTestExecutionListener.class})
    public void testGenerateMavenChecksumForSnapshotArtifact(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                              policy = SNAPSHOT)
                                                             Repository repository,
                                                             @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                                resource = S2)
                                                             Path artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        // JAR MD5 file.
        String fileName1 = artifact.getFileName().toString();
        String checksumFileName1 = fileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact1Md5 = artifact.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1Md5);

        // JAR SHA1 file.
        checksumFileName1 = fileName1 + ".sha1";
        Path artifact1Sha1 = artifact.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1Sha1);

        // POM MD5 file.
        String pomFileName1 = fileName1.replace("jar", "pom");
        checksumFileName1 = pomFileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifact1PomMd5 = artifact.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1PomMd5);

        // POM SHA1 file.
        checksumFileName1 = pomFileName1 + ".sha1";
        Path artifact1PomSha1 = artifact.resolveSibling(checksumFileName1);
        deleteIfExists(artifact1PomSha1);

        // Metadata XML MD5 file.
        String metadataXmlFileName = "maven-metadata.xml";
        checksumFileName1 = metadataXmlFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactMetadataMd5 = artifact.getParent().getParent().resolve(checksumFileName1);

        // Metadata XML SHA1 file.
        checksumFileName1 = metadataXmlFileName + ".sha1";
        Path artifactMetadataSha1 = artifact.getParent().getParent().resolve(checksumFileName1);

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                "org/carlspring/strongbox/checksum");

        assertThat(Files.exists(artifact1Md5))
                .as("The checksum file for artifact exist!")
                .isFalse();
        assertThat(Files.exists(artifact1Sha1))
                .as("The checksum file for artifact exist!")
                .isFalse();

        checksumService.regenerateChecksum(storageId,
                                           repositoryId,
                                           "org/carlspring/strongbox/checksum",
                                           false);
       
        assertThat(Files.exists(artifact1Sha1))
                .as("The checksum file for the artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1Sha1) > 0)
                .as("The checksum file for the artifact is empty!")
                .isTrue();
        assertThat(Files.exists(artifact1Md5))
                .as("The checksum file for the artifact doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1Md5) > 0)
                .as("The checksum file for the artifact is empty!")
                .isTrue();

        assertThat(Files.exists(artifact1PomSha1))
                .as("The checksum file for the pom file doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1PomSha1) > 0)
                .as("The checksum file for the pom file is empty!")
                .isTrue();
        assertThat(Files.exists(artifact1PomMd5))
                .as("The checksum file for the pom file doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifact1PomMd5) > 0)
                .as("The checksum file for the pom file is empty!")
                .isTrue();
        
        assertThat(Files.exists(artifactMetadataMd5))
                .as("The checksum file for metadata doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifactMetadataMd5) > 0)
                .as("The checksum file for metadata is empty!")
                .isTrue();
        assertThat(Files.exists(artifactMetadataSha1))
                .as("The checksum file for metadata doesn't exist!")
                .isTrue();
        assertThat(Files.size(artifactMetadataSha1) > 0)
                .as("The checksum file for metadata is empty!")
                .isTrue();
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class,
                 ArtifactManagementTestExecutionListener.class})
    public void testRewriteMavenChecksum(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                         Repository repository,
                                         @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                            resource = A3)
                                         Path artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                "org/carlspring/strongbox/checksum");

        // JAR MD5 file.
        String fileName1 = artifact.getFileName().toString();
        String checksumFileName1 = fileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path md5File = artifact.resolveSibling(checksumFileName1);
        assertThat(Files.exists(md5File)).as("The checksum file for artifact doesn't exist!").isTrue();

        // JAR SHA1 file.
        checksumFileName1 = fileName1 + ".sha1";
        Path sha1File = artifact.resolveSibling(checksumFileName1);
        assertThat(Files.exists(sha1File)).as("The checksum file for artifact file doesn't exist!").isTrue();

        // POM MD5 file.
        String pomFileName1 = fileName1.replace("jar", "pom");
        checksumFileName1 = pomFileName1 + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path pomMd5 = artifact.resolveSibling(checksumFileName1);
        assertThat(Files.exists(pomMd5)).as("The checksum file for pom file doesn't exist!").isTrue();

        // POM SHA1 file.
        checksumFileName1 = pomFileName1 + ".sha1";
        Path pomSha1 = artifact.resolveSibling(checksumFileName1);
        assertThat(Files.exists(pomSha1)).as("The checksum file for pom file doesn't exist!").isTrue();

        // Metadata XML MD5 file.
        String metadataXmlFileName = "maven-metadata.xml";
        checksumFileName1 = metadataXmlFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactMetadataMd5 = artifact.getParent().getParent().resolve(checksumFileName1);
        assertThat(Files.exists(artifactMetadataMd5)).as("The checksum file for metadata doesn't exist!").isTrue();

        // Metadata XML SHA1 file.
        checksumFileName1 = metadataXmlFileName + ".sha1";
        Path artifactMetadataSha1 = artifact.getParent().getParent().resolve(checksumFileName1);
        assertThat(Files.exists(artifactMetadataSha1)).as("The checksum file for metadata doesn't exist!").isTrue();

        try (
                OutputStream os1 = Files.newOutputStream(md5File, CREATE_NEW, TRUNCATE_EXISTING);
                OutputStream os2 = Files.newOutputStream(sha1File, CREATE_NEW, TRUNCATE_EXISTING);
                OutputStream os3 = Files.newOutputStream(pomMd5, CREATE_NEW, TRUNCATE_EXISTING);
                OutputStream os4 = Files.newOutputStream(pomSha1, CREATE_NEW, TRUNCATE_EXISTING);
                OutputStream os5 = Files.newOutputStream(artifactMetadataMd5, CREATE_NEW, TRUNCATE_EXISTING);
                OutputStream os6 = Files.newOutputStream(artifactMetadataSha1, CREATE_NEW, TRUNCATE_EXISTING)
        )
        {
            os1.write("".getBytes());
            os1.flush();
            os2.write("".getBytes());
            os2.flush();
            os3.write("".getBytes());
            os3.flush();
            os4.write("".getBytes());
            os4.flush();
            os5.write("".getBytes());
            os5.flush();
            os6.write("".getBytes());
            os6.flush();
        }

        assertThat(Files.size(md5File)).as("The checksum file for artifact isn't empty!").isEqualTo(0);
        assertThat(Files.size(pomSha1))
                .as("The checksum file for pom file isn't empty!")
                .isEqualTo(0);
        assertThat(Files.size(artifactMetadataMd5))
                .as("The checksum file for metadata isn't empty!")
                .isEqualTo(0);

        checksumService.regenerateChecksum(storageId,
                                           repositoryId,
                                           "org/carlspring/strongbox/checksum/maven/checksum-rewrite",
                                           true);

        assertThat(Files.size(md5File) > 0).as("The checksum file for artifact is empty!").isTrue();
        assertThat(Files.size(pomSha1) > 0)
                .as("The checksum file for pom file is empty!")
                .isTrue();
        assertThat(Files.size(artifactMetadataMd5) > 0)
                .as("The checksum file for metadata is empty!")
                .isTrue();
    }

}
