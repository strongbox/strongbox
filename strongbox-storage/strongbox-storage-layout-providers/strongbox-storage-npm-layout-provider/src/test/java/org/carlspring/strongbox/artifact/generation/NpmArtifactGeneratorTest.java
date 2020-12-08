package org.carlspring.strongbox.artifact.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import org.carlspring.strongbox.config.NpmLayoutProviderTestConfig;
import org.carlspring.strongbox.npm.metadata.License;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

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
                                                 bytesSize = 2048)
                                Path path)
            throws Exception
    {
        assertArtifactAndHash(path);
        
        try (InputStream is = Files.newInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(is);
             GzipCompressorInputStream gzInputStream = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzInputStream))
        {
            PackageVersion packageJson = null;
            TarArchiveEntry entry = null;
            TarArchiveEntry apacheLicenseEntry = null;
            TarArchiveEntry licenseEntry = null;
            TarArchiveEntry packageJsonEntry = null;
            while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null)
            {
                if (entry.getName().equals("LICENSE-Apache-2.0.md"))
                {
                    apacheLicenseEntry = entry;
                }
                else if (entry.getName().equals("LICENSE"))
                {
                    licenseEntry = entry;
                }
                else if (entry.getName().equals("package.json"))
                {
                    packageJsonEntry = entry;
                    packageJson = new ObjectMapper().readValue(new String(IOUtils.toByteArray(tarArchiveInputStream)),
                                                               PackageVersion.class);
                }
            }

            assertThat(packageJsonEntry)
                    .as("Did not find a license file package.json was expected at the default location in the TAR source package!")
                    .isNotNull();

            assertThat(apacheLicenseEntry)
                    .as("Found license file LICENSE-Apache-2.0.md that was not expected at the default location in the TAR source package!")
                    .isNull();

            assertThat(licenseEntry)
                    .as("Found license file LICENSE that was expected at the default location in the TAR source package!")
                     .isNull();

            assertThat(packageJson.getLicenses().size())
                    .isEqualTo(0);
        }
    }
    
    
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testArtifactGenerationWithLicense(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                           Repository repository,
                                           @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                            id = "npm-test-view-with-license",
                                                            versions = "1.0.0",
                                                            scope = "@carlspring",
                                                            bytesSize = 2048,
                                                            licenses = { @LicenseConfiguration(license = LicenseType.APACHE_2_0,
                                                                                               destinationPath = "LICENSE-Apache-2.0.md"),
                                                                         @LicenseConfiguration(license = LicenseType.MIT,
                                                                                               destinationPath = "LICENSE-MIT-License.md")})
                                           Path path)
            throws Exception
    {
        assertArtifactAndHash(path);
        
        try (InputStream is = Files.newInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(is);
             GzipCompressorInputStream gzInputStream = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzInputStream))
        {
            PackageVersion packageJson = null;
            TarArchiveEntry entry = null;
            TarArchiveEntry apacheLicenseEntry = null;
            TarArchiveEntry mitLicenseEntry = null;
            TarArchiveEntry packageJsonEntry = null;
            while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null)
            {
                if (entry.getName().equals("LICENSE-Apache-2.0.md"))
                {
                    apacheLicenseEntry = entry;
                }
                else if (entry.getName().equals("LICENSE-MIT-License.md"))
                {
                    mitLicenseEntry = entry;
                }
                else if (entry.getName().equals("package.json"))
                {
                    packageJsonEntry = entry;
                    packageJson = new ObjectMapper().readValue(new String(IOUtils.toByteArray(tarArchiveInputStream)),
                                                               PackageVersion.class);
                }
            }

            assertThat(packageJsonEntry)
                    .as("Did not find a license file package.json was expected at the default location in the TAR source package!")
                    .isNotNull();
            
            assertThat(apacheLicenseEntry)
                    .as("Did not find a license file LICENSE-Apache-2.0.md that was expected at the default location in the TAR source package!")
                    .isNotNull();
            
            assertThat(mitLicenseEntry)
                    .as("Did not find a license file LICENSE-MIT-License.md that was expected at the default location in the TAR source package!")
                    .isNotNull();
            
            assertThat(packageJson.getLicenses())
                    .isNotNull()
                    .as("licenses key value not present in package.json!");
            
            List<String> licenses = packageJson.getLicenses()
                                               .stream()
                                               .map(License::getType)
                                               .collect(Collectors.toList());
            
            assertThat(licenses).contains("Apache 2.0", "MIT License");
        }
    }

    private void assertArtifactAndHash(Path path)
            throws IOException,
                   NoSuchAlgorithmException
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
