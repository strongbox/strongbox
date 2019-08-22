package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
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
        Path artifactJarPathMd5 = Paths.get(artifactPath.toString() + ".md5");

        Path artifactJarPathSha1 = Paths.get(artifactPath.toString() + ".sha1");

        Path artifactPomPath = Paths.get(artifactPath.toString().replaceAll("jar", "pom"));
        Path artifactPomPathMd5 = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".md5");
        Path artifactPomPathSha1 = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".sha1");

        assertTrue(Files.exists(artifactPath), "Failed to generate JAR file!");
        assertTrue(Files.exists(artifactJarPathMd5), "Failed to generate JAR MD5 file!");
        assertTrue(Files.exists(artifactJarPathSha1), "Failed to generate JAR SHA1 file!");

        assertTrue(Files.exists(artifactPomPath), "Failed to generate POM file!");
        assertTrue(Files.exists(artifactPomPathMd5), "Failed to generate POM MD5 file!");
        assertTrue(Files.exists(artifactPomPathSha1), "Failed to generate POM SHA1 file!");

        String expectedJarMD5 = calculateChecksum(artifactPath, "MD5");
        String expectedJarSHA1 = calculateChecksum(artifactPath, "SHA1");

        String jarMd5 = MessageDigestUtils.readChecksumFile(artifactJarPathMd5.toString());
        String jarSha1 = MessageDigestUtils.readChecksumFile(artifactJarPathSha1.toString());

        System.out.println("Expected  [MD5 ] (jar): " + expectedJarMD5);
        System.out.println("Generated [MD5 ] (jar): " + jarMd5);

        assertEquals(expectedJarMD5, jarMd5);

        System.out.println("Expected  [SHA1] (jar): " + expectedJarSHA1);
        System.out.println("Generated [SHA1] (jar): " + jarSha1);

        assertEquals(expectedJarSHA1, jarSha1);

        String expectedPomMD5 = calculateChecksum(artifactPomPath, "MD5");
        String expectedPomSHA1 = calculateChecksum(artifactPomPath, "SHA1");

        String pomMD5 = MessageDigestUtils.readChecksumFile(artifactPomPathMd5.toString());
        String pomSHA1 = MessageDigestUtils.readChecksumFile(artifactPomPathSha1.toString());

        System.out.println("Expected  [MD5 ] (pom): " + expectedPomMD5);
        System.out.println("Generated [MD5 ] (pom): " + pomMD5);

        assertEquals(expectedPomMD5, pomMD5);

        System.out.println("Expected  [SHA1] (pom): " + expectedPomSHA1);
        System.out.println("Generated [SHA1] (pom): " + pomSHA1);

        assertEquals(expectedPomSHA1, pomSHA1);
    }

}
