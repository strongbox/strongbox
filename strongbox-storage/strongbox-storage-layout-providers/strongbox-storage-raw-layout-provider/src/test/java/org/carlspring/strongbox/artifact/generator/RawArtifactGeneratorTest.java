package org.carlspring.strongbox.artifact.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
public class RawArtifactGeneratorTest
{

    private static final String RAW_REPOSITORY = "raw-releases";

    private static final String TXT_ARTIFACT = "org/carlspring/strongbox/raw/hello.txt";
    private static final String JAR_ARTIFACT = "org/carlspring/strongbox/raw/foo.jar";
    private static final String ZIP_ARTIFACT = "org/carlspring/strongbox/raw/bar.zip";

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRawTxtFile(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                               repositoryId = RAW_REPOSITORY)
                               Repository repository,
                               @TestArtifact(resource = TXT_ARTIFACT,
                                             generator = RawArtifactGenerator.class,
                                             bytesSize = 2048)
                               Path artifactPath)
        throws Exception
    {
        assertThat(Files.exists(artifactPath)).isTrue().as("Failed to generate txt artifact.");
        assertThat(artifactPath.getFileName().toString()).isEqualTo("hello.txt");

        assertGeneratedArtifact(artifactPath, 2048);
    }
    

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRawJarFile(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                               repositoryId = RAW_REPOSITORY)
                               Repository repository,
                               @TestArtifact(resource = JAR_ARTIFACT,
                                             generator = RawArtifactGenerator.class,
                                             bytesSize = 4096)
                               Path artifactPath)
        throws Exception
    {
        assertThat(Files.exists(artifactPath)).isTrue().as("Failed to generate jar file.");
        assertThat(artifactPath.getFileName().toString()).isEqualTo("foo.jar");

        assertGeneratedArtifact(artifactPath, 4096);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRawZipFile(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                               repositoryId = RAW_REPOSITORY)
                               Repository repository,
                               @TestArtifact(resource = ZIP_ARTIFACT,
                                             generator = RawArtifactGenerator.class,
                                             bytesSize = 10240)
                               Path artifactPath)
        throws Exception
    {
        assertThat(Files.exists(artifactPath)).isTrue().as("Failed to generate zip file.");
        assertThat(artifactPath.getFileName().toString()).isEqualTo("bar.zip");

        assertGeneratedArtifact(artifactPath, 10240);
    }
    
    private void assertGeneratedArtifact(Path artifactPath,
                                         int bytesSize)
        throws NoSuchAlgorithmException,
        IOException
    {
        String checksumFileName = artifactPath.getFileName().toString() + "."
                + MessageDigestAlgorithms.MD5.toLowerCase();
        Path checksumFilePath = artifactPath.resolveSibling(checksumFileName);

        assertThat(Files.exists(checksumFilePath)).isTrue()
                                                  .as("Failed to generate md5 checksum file for raw artifact.");

        String expectedChecksum = calculateChecksum(artifactPath, MessageDigestAlgorithms.MD5);
        String actualChecksum = readChecksumFile(checksumFilePath.toString());

        assertThat(actualChecksum).isEqualTo(expectedChecksum);

        assertThat(Files.size(artifactPath)).isGreaterThan(bytesSize);
    }
}
