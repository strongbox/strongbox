package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.NpmLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Wojciech Pater
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NpmLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
class NpmArtifactGeneratorTest
{

    private static final String REPOSITORY_RELEASES = "natg-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testArtifactGeneration(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                        Repository repository,
                                @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                        id = "npm-test-view",
                                        versions = "1.0.0",
                                        scope = "@carlspring",
                                        size = 2048)
                                        Path path)
            throws Exception
    {
        assertThat(Files.exists(path)).as("Failed to generate NPM package.").isTrue();

        // SHA1
        String fileName = path.getFileName().toString();
        String checksumFileName = fileName + ".sha1";
        Path pathSha1 = path.resolveSibling(checksumFileName);
        assertThat(Files.exists(pathSha1)).as("Failed to generate NPM SHA1 file.").isTrue();

        String expectedSha1 = calculateChecksum(path, MessageDigestAlgorithms.SHA_1);
        String result = readChecksumFile(pathSha1.toString());
        assertThat(result).isEqualTo(expectedSha1);

        // File size
        assertThat(Files.size(path)).isGreaterThan(2048);
    }
}
