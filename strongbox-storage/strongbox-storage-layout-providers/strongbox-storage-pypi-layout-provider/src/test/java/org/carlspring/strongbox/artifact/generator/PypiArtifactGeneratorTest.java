package org.carlspring.strongbox.artifact.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
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
                                              bytesSize = 4096,
                                              licenses = { @LicenseConfiguration(license = LicenseType.MIT,
                                                                                 destinationPath = "LICENSE-mit.md") })
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
        
        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry licenseFile = zipFile.getEntry("org-carlspring.dist-info/LICENSE-mit.md");
            ZipEntry metadataFile = zipFile.getEntry("org-carlspring.dist-info/METADATA");

            assertThat(metadataFile)
                    .as("Metadata File METADATA not present in whl package!")
                    .isNotNull();

            assertThat(licenseFile)
                    .as("Did not find a license file LICENSE-mit.md that was expected at the default location in the whl package!")
                    .isNotNull();

            try (InputStreamReader streamReader = new InputStreamReader(zipFile.getInputStream(metadataFile));
                 BufferedReader bufferedReader = new BufferedReader(streamReader);)
            {
                String metadataContent = bufferedReader.lines().collect(Collectors.joining("\n"));
                assertThat(metadataContent).contains("License: MIT License");
            }
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testTARFile(@PypiTestRepository(repositoryId = "repositoryId")
                            Repository repository,
                            @PypiTestArtifact(repositoryId = "repositoryId",
                                              resource = "strongbox-testing-1.0.tar.gz",
                                              bytesSize = 4096,
                                              licenses = { @LicenseConfiguration(license = LicenseType.APACHE_2_0,
                                                                                 destinationPath = "LICENSE-Apache-2.0.md") })
                            Path artifactPath)
            throws Exception
    {
        assertArtifactAndHash(artifactPath);

        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry licenseFile = zipFile.getEntry("LICENSE-Apache-2.0.md");
            ZipEntry metadataFile = zipFile.getEntry("PKG-INFO");

            assertThat(metadataFile)
                    .as("Metadata File PKG-INFO not present in TAR source package!")
                    .isNotNull();
            
            assertThat(licenseFile)
                    .as("Did not find a license file LICENSE-Apache-2.0.md that was expected at the default location in the TAR source package!")
                    .isNotNull();
            
            try (InputStreamReader streamReader = new InputStreamReader(zipFile.getInputStream(metadataFile));
                 BufferedReader bufferedReader = new BufferedReader(streamReader);)
            {
                String metadataContent = bufferedReader.lines().collect(Collectors.joining("\n"));
                assertThat(metadataContent).contains("License: Apache 2.0");
            }
        }
    }

    
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactWithoutLicense(@PypiTestRepository(repositoryId = "repositoryId")
                                           Repository repository,
                                           @PypiTestArtifact(repositoryId = "repositoryId",
                                                             resource = "strongbox-testing-without-license-1.0.tar.gz",
                                                             bytesSize = 4096)
                                           Path artifactPath)
            throws Exception
    {
        assertArtifactAndHash(artifactPath);

        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry licenseFile = zipFile.getEntry("LICENSE.md");
            ZipEntry metadataFile = zipFile.getEntry("PKG-INFO");

            assertThat(metadataFile)
                    .as("Metadata File PKG-INFO not present in TAR source package!")
                    .isNotNull();
            
            assertThat(licenseFile)
                    .as("Found LICENSE.md that was not expected at the default location in the TAR source package!")
                    .isNull();
            
            try (InputStreamReader streamReader = new InputStreamReader(zipFile.getInputStream(metadataFile));
                 BufferedReader bufferedReader = new BufferedReader(streamReader);)
            {
                String metadataContent = bufferedReader.lines().collect(Collectors.joining("\n"));
                assertThat(metadataContent).contains("License: Unlicense");
            }
        }
    }

    private void assertArtifactAndHash(Path artifactPath)
            throws IOException,
                   NoSuchAlgorithmException
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
