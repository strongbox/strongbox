package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
                                                          versions = "1.2.3",
                                                          bytesSize = 2048,
                                                          licenses = { @LicenseConfiguration(license = LicenseType.APACHE_2_0,
                                                                                             destinationPath = "META-INF/LICENSE-Apache-2.0.md"),
                                                                       @LicenseConfiguration(license = LicenseType.MIT) })
                                       Path artifactPath)
            throws Exception
    {
        assertThat(Files.exists(artifactPath)).as("Failed to generate JAR file!").isTrue();

        // JAR MD5 file.
        String fileName = artifactPath.getFileName().toString();
        String checksumFileName = fileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactJarPathMd5 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(artifactJarPathMd5)).as("Failed to generate JAR MD5 file!").isTrue();

        String expectedJarMD5 = calculateChecksum(artifactPath, MessageDigestAlgorithms.MD5);
        String jarMd5 = readChecksumFile(artifactJarPathMd5.toString());
        assertThat(jarMd5).isEqualTo(expectedJarMD5);

        // JAR SHA1 file.
        checksumFileName = fileName + ".sha1";
        Path artifactJarPathSha1 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(artifactJarPathSha1)).as("Failed to generate JAR SHA1 file!").isTrue();

        String expectedJarSHA1 = calculateChecksum(artifactPath, MessageDigestAlgorithms.SHA_1);
        String jarSha1 = readChecksumFile(artifactJarPathSha1.toString());
        assertThat(jarSha1).isEqualTo(expectedJarSHA1);

        // POM file.
        String pomFileName = fileName.replace("jar", "pom");
        Path artifactPomPath = artifactPath.resolveSibling(pomFileName);
        assertThat(Files.exists(artifactPomPath)).as("Failed to generate POM file!").isTrue();

        // POM MD5 file.
        checksumFileName = pomFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactPomPathMd5 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(artifactPomPathMd5)).as("Failed to generate POM MD5 file!").isTrue();

        String expectedPomMD5 = calculateChecksum(artifactPomPath, MessageDigestAlgorithms.MD5);
        String pomMD5 = readChecksumFile(artifactPomPathMd5.toString());
        assertThat(pomMD5).isEqualTo(expectedPomMD5);

        // POM SHA1 file.
        checksumFileName = pomFileName + ".sha1";
        Path artifactPomPathSha1 = artifactPath.resolveSibling(checksumFileName);
        assertThat(Files.exists(artifactPomPathSha1)).as("Failed to generate POM SHA1 file!").isTrue();

        String expectedPomSHA1 = calculateChecksum(artifactPomPath, MessageDigestAlgorithms.SHA_1);
        String pomSHA1 = readChecksumFile(artifactPomPathSha1.toString());
        assertThat(pomSHA1).isEqualTo(expectedPomSHA1);

        // JAR size
        assertThat(Files.size(artifactPath)).isGreaterThan(2048);

        // License checks
        checkLicenses(artifactPath);
    }

    public void checkLicenses(Path artifactPath)
            throws IOException, XmlPullParserException
    {
        try (JarFile jf = new JarFile(artifactPath.toFile());)
        {
            // 1) Check that the license are located in the expected locations
            assertThat(jf.getJarEntry("META-INF/LICENSE-Apache-2.0.md"))
                    .as("Did not find a license that was expected at specified location in the JAR!")
                    .isNotNull();

            assertThat(jf.getJarEntry("LICENSE"))
                    .as("Did not find a license that was expected at the default location in the JAR!")
                    .isNotNull();

            // 2) Check that the POM contains the list of licenses
            JarEntry pomEntry = jf.getJarEntry("META-INF/maven/org.carlspring.strongbox.testing/matg/pom.xml");

            assertThat(pomEntry).as("Did not find a POM inside the JAR!").isNotNull();

            MavenXpp3Reader reader = new MavenXpp3Reader();

            try (InputStream is = jf.getInputStream(pomEntry))
            {
                Model model = reader.read(is);
                List<License> licenses = model.getLicenses();

                assertThat(licenses).as("Could not discover any licenses in the POM file!").isNotNull();
                assertThat(licenses.size()).as("Could not discover any licenses in the POM file!").isEqualByComparingTo(2);

                assertThat(licenses.get(0).getName()).as("Failed to locate a definition for the 'Apache 2.0' license in the POM file!")
                                                     .isEqualTo("Apache 2.0");
                assertThat(licenses.get(1).getName()).as("Failed to locate a definition for the 'MIT License' license in the POM file!")
                                                     .isEqualTo("MIT License");
            }
        }
    }

}
