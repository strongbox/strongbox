package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.config.RpmLayoutProviderTestConfig;
import org.carlspring.strongbox.domain.RpmPackageArch;
import org.carlspring.strongbox.domain.RpmPackageType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.RpmTestArtifact;
import org.carlspring.strongbox.testing.repository.RpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

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
 * @author carlspring
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RpmLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
class RpmArtifactGeneratorTest
{

    private static final String REPOSITORY_RELEASES = "rpmatg-releases";

//    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
//                  ArtifactManagementTestExecutionListener.class })
//    @Test
//    void testArtifactGeneration(@RpmRepository(repositoryId = REPOSITORY_RELEASES)
//                                Repository repository,
//                                @RpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
//                                                 id = "rpm-test-view",
//                                                 versions = "1.0.0",
//                                                 scope = "@carlspring",
//                                                 bytesSize = 2048)
//                                Path path)
//            throws Exception
//    {
//        assertThat(Files.exists(path)).as("Failed to generate RPM package.").isTrue();
//
//        // SHA1
//        String fileName = path.getFileName().toString();
//        String checksumFileName = fileName + ".sha1";
//        Path pathSha1 = path.resolveSibling(checksumFileName);
//        assertThat(Files.exists(pathSha1)).as("Failed to generate RPM SHA1 file.").isTrue();
//
//        String expectedSha1 = calculateChecksum(path, MessageDigestAlgorithms.SHA_1);
//        String result = readChecksumFile(pathSha1.toString());
//        assertThat(result).isEqualTo(expectedSha1);
//
//        // File size
//        assertThat(Files.size(path)).isGreaterThan(2048);
//    }

    @Test
    public void testBasicRpmArtifactGeneration()
            throws IOException
    {
        String basedir = "target/strongbox-vault/storages/storage-rpm/rpm-releases";

        Path path = Paths.get(basedir);

        if (!Files.exists(path))
        {
            Files.createDirectories(path);
        }

        RpmArtifactCoordinates coordinates = new RpmArtifactCoordinates("test",
                                                                        "1.0.0",
                                                                        "1",
                                                                        RpmPackageType.BINARY,
                                                                        RpmPackageArch.NOARCH);

        Path basePath = Paths.get(basedir);
        Path packagePath = Paths.get(coordinates.toPath());

        RpmArtifactGenerator generator = new RpmArtifactGenerator(path);
        generator.setCoordinates(coordinates);
        generator.setBasePath(basePath);
        generator.setPackagePath(packagePath);
        generator.generate(1024);

        assertThat(Paths.get(basePath.toString(), packagePath.toString())).exists();
    }

}
