package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenArtifactGeneratorTest
{

    private static final String REPOSITORY_RELEASES = "matg-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactGeneration(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                          id = "org.carlspring.strongbox.testing:matg",
                                                          versions = "1.2.3")
                                       Path artifactPath)
            throws Exception
    {
        assertTrue(Files.exists(artifactPath), "Failed to generate JAR file!");

        // JAR MD5 file.
        String fileName = artifactPath.getFileName().toString();
        String checksumFileName = fileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactJarPathMd5 = artifactPath.resolveSibling(checksumFileName);
        assertTrue(Files.exists(artifactJarPathMd5), "Failed to generate JAR MD5 file!");

        String expectedJarMD5 = calculateChecksum(artifactPath, MessageDigestAlgorithms.MD5);
        String jarMd5 = readChecksumFile(artifactJarPathMd5.toString());
        assertEquals(expectedJarMD5, jarMd5);

        // JAR SHA1 file.
        checksumFileName = fileName + ".sha1";
        Path artifactJarPathSha1 = artifactPath.resolveSibling(checksumFileName);
        assertTrue(Files.exists(artifactJarPathSha1), "Failed to generate JAR SHA1 file!");

        String expectedJarSHA1 = calculateChecksum(artifactPath, MessageDigestAlgorithms.SHA_1);
        String jarSha1 = readChecksumFile(artifactJarPathSha1.toString());
        assertEquals(expectedJarSHA1, jarSha1);

        // POM file.
        String pomFileName = fileName.replace("jar", "pom");
        Path artifactPomPath = artifactPath.resolveSibling(pomFileName);
        assertTrue(Files.exists(artifactPomPath), "Failed to generate POM file!");

        // POM MD5 file.
        checksumFileName = pomFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactPomPathMd5 = artifactPath.resolveSibling(checksumFileName);
        assertTrue(Files.exists(artifactPomPathMd5), "Failed to generate POM MD5 file!");

        String expectedPomMD5 = calculateChecksum(artifactPomPath, MessageDigestAlgorithms.MD5);
        String pomMD5 = readChecksumFile(artifactPomPathMd5.toString());
        assertEquals(expectedPomMD5, pomMD5);

        // POM SHA1 file.
        checksumFileName = pomFileName + ".sha1";
        Path artifactPomPathSha1 = artifactPath.resolveSibling(checksumFileName);
        assertTrue(Files.exists(artifactPomPathSha1), "Failed to generate POM SHA1 file!");

        String expectedPomSHA1 = calculateChecksum(artifactPomPath, MessageDigestAlgorithms.SHA_1);
        String pomSHA1 = readChecksumFile(artifactPomPathSha1.toString());
        assertEquals(expectedPomSHA1, pomSHA1);
    }

}
