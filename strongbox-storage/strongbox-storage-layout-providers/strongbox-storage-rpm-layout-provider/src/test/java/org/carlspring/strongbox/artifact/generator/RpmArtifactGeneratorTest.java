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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
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


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testArtifactGeneration(@RpmRepository(repositoryId = REPOSITORY_RELEASES)
                                Repository repository,
                                @RpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                 id = "libqxt-qt5",
                                                 versions = "0.7.0",
                                                 bytesSize = 2048)
                                Path path)
    {
        assertThat(repository).isNotNull();

        System.out.println(repository.getStorage().getId() + ":" + repository.getId());
        System.out.println(path.toAbsolutePath().toString());

        assertThat(repository).as("Failed to create an RPM repository!").isNotNull();
        assertThat(path).as("Failed to generate RPM artifact!").exists();

        assertThat(Files.exists(path)).as("Failed to generate RPM package.").isTrue();
    }

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
        generator.generate(1024);

        assertThat(Paths.get(basePath.toString(), packagePath.toString())).exists();
    }

}
