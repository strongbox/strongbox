package org.carlspring.strongbox.artifact.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
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

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
public class NugetArtifactGeneratorTest
{

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testNupkgPackageWithoutLicense(@NugetRepository(repositoryId = "nagt-releases") 
                                               Repository repository,
                                               @NugetTestArtifact(repositoryId = "nagt-releases",
                                                                  id = "nuget-artifact-without-license",
                                                                  versions = "1.0.0",
                                                                  bytesSize = 4096)
                                               Path artifactPath)
            throws Exception
    {

        assertThat(Files.size(artifactPath)).isGreaterThan(4096);
        assertArtifactAndFiles(artifactPath, "nuget-artifact-without-license");

        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry licenseFile = zipFile.getEntry("licenses/LICENSE.md");
            ZipEntry metadataFile = zipFile.getEntry("nuget-artifact-without-license.nuspec");

            assertThat(metadataFile)
                    .as("Metadata File nuget-artifact-without-license.nuspec not present in source package!")
                    .isNotNull();

            assertThat(licenseFile)
                    .as("Found LICENSE.md that was not expected at the default location in the source package!")
                    .isNull();

            try (InputStreamReader streamReader = new InputStreamReader(zipFile.getInputStream(metadataFile));
                 BufferedReader bufferedReader = new BufferedReader(streamReader);)
            {
                String metadataContent = bufferedReader.lines()
                                                       .collect(Collectors.joining("\n"));
                assertThat(metadataContent).doesNotContain("<licenseUrl>");
            }
        }
    }
    
    
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testNupkgPackageWithLicense(@NugetRepository(repositoryId = "nagt-releases") 
                                         Repository repository,
                                         @NugetTestArtifact(repositoryId = "nagt-releases",
                                                            bytesSize = 4096,
                                                            id = "nuget-artifact-with-license",
                                                            versions = "1.0.0",
                                                            licenses = { @LicenseConfiguration(license = LicenseType.MIT,
                                                                                               destinationPath = "licenses/MIT-License.md") })
                                         Path artifactPath)
      throws Exception
    {
        
        assertThat(Files.size(artifactPath)).isGreaterThan(4096);
        assertArtifactAndFiles(artifactPath, "nuget-artifact-with-license");

        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry licenseFile = zipFile.getEntry("licenses/MIT-License.md");
            ZipEntry metadataFile = zipFile.getEntry("nuget-artifact-with-license.nuspec");

            assertThat(metadataFile)
                    .as("Metadata File nuget-artifact-with-license.nuspec not present in source package!")
                    .isNotNull();

            assertThat(licenseFile)
                    .as("Did not find a license file MIT-License.md that was expected at the location lincenses/ in the source package!")
                    .isNotNull();

            try (InputStreamReader streamReader = new InputStreamReader(zipFile.getInputStream(metadataFile));
                 BufferedReader bufferedReader = new BufferedReader(streamReader);)
            {
                String metadataContent = bufferedReader.lines()
                                                       .collect(Collectors.joining("\n"));
                assertThat(metadataContent).contains("<licenseUrl>https://opensource.org/licenses/MIT</licenseUrl>");
            }
        }
    }
    
    private void assertArtifactAndFiles(Path artifactPath,
                                        String nuspecFileName)
             throws IOException
    {
        assertThat(Files.exists(artifactPath)).as("Failed to generate nupkg file!").isTrue();

        try (ZipFile zipFile = new ZipFile(artifactPath.toFile()))
        {
            ZipEntry psmdcpFile = zipFile.getEntry("package/services/metadata/core-properties/metadata.psmdcp");
            ZipEntry nuspecFile = zipFile.getEntry(nuspecFileName + ".nuspec");
            ZipEntry contentTypeFile = zipFile.getEntry("[Content_Types].xml");
            ZipEntry relFile = zipFile.getEntry("_rels/.rels");

            assertThat(psmdcpFile)
                    .as("Did not find metadata.psmdcp file in the source package!")
                    .isNotNull();
            
            assertThat(nuspecFile)
                    .as("Did not find a nuspecFile license file in the source package!")
                    .isNotNull();
            
            assertThat(contentTypeFile)
                    .as("Did not find [Content_Types].xml in the source package!")
                    .isNotNull();
            
            assertThat(relFile)
                    .as("Did not find a _rels/.rels in the source package!")
                    .isNotNull();
        }
    }
}
