package org.carlspring.strongbox.artifact.generator;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@Transactional
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = PypiLayoutProviderTestConfig.class)
public class PypiArtifactGeneratorTest
{

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testWHLFile(@PypiTestRepository(repositoryId = "repositoryId")
                            Repository repository,
                            @PypiTestArtifact(repositoryId = "repositoryId",
                                              resource = "org-carlspring-123-strongbox-testing-pypi.whl",
                                              bytesSize = 4096)
                            Path artifactPath)
            throws Exception
    {
        assertThat(Files.exists(artifactPath)).as("Failed to generate WHL file!").isTrue();

        String fileName = artifactPath.getFileName().toString();
        String checksumFileName = fileName + ".sha256";
        Path pathSha256 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(pathSha256)).as("Failed to generate WHL SHA256 file.").isTrue();

        String expectedSha1 = calculateChecksum(artifactPath, MessageDigestAlgorithms.SHA_256);
        String result = readChecksumFile(pathSha256.toString());
        assertThat(result).isEqualTo(expectedSha1);

        assertThat(Files.size(artifactPath)).isGreaterThan(4096);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testTARFile(@PypiTestRepository(repositoryId = "repositoryId")
                            Repository repository,
                            @PypiTestArtifact(repositoryId = "repositoryId",
                                              resource = "strongbox-testing-1.0.tar.gz",
                                              bytesSize = 4096)
                            Path artifactPath)
            throws Exception
    {
        assertThat(Files.exists(artifactPath)).as("Failed to generate TAR file!").isTrue();

        String fileName = artifactPath.getFileName().toString();
        String checksumFileName = fileName + ".sha256";
        Path pathSha256 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(pathSha256)).as("Failed to generate TAR SHA256 file.").isTrue();

        String expectedSha1 = calculateChecksum(artifactPath, MessageDigestAlgorithms.SHA_256);
        String result = readChecksumFile(pathSha256.toString());
        assertThat(result).isEqualTo(expectedSha1);

        assertThat(Files.size(artifactPath)).isGreaterThan(4096);
    }

}
