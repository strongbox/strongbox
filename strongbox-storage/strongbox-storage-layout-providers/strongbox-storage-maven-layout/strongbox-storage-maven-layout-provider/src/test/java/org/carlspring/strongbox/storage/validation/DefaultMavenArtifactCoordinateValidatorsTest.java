package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.carlspring.strongbox.storage.validation.version.MavenReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.version.MavenSnapshotVersionValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { Maven2LayoutProviderTestConfig.class})
@Execution(CONCURRENT)
public class DefaultMavenArtifactCoordinateValidatorsTest
{

    private static final String TEST_CLASSES = "target/test-classes";

    private static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    private static final String STORAGE0 = "storage0";

    private static final String REPOSITORY_RELEASES = "releases";

    private static final String REPOSITORY_SNAPSHOTS = "snapshots";


    @Inject
    private ConfigurationManager configurationManager;


    @BeforeEach
    public void setUp()
            throws IOException
    {
        Path xmlPath = Paths.get(CONFIGURATION_BASEDIR);
        if (Files.notExists(xmlPath))
        {
            Files.createDirectories(xmlPath);
        }
    }

    @Test
    public void testParseConfiguration()
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertThat(configuration).isNotNull();
        assertThat(configuration.getStorages()).isNotNull();
        assertThat(configuration.getRoutingRules()).isNotNull();

        for (String storageId : configuration.getStorages().keySet())
        {
            assertThat(storageId).as("Storage ID was null!").isNotNull();
        }

        assertThat(configuration.getStorages()).as("Unexpected number of storages!").isNotEmpty();
        assertThat(configuration.getVersion()).as("Incorrect version!").isNotNull();
        assertThat(configuration.getPort()).as("Incorrect port number!").isEqualTo(48080);
        assertThat(configuration.getStorages()
                                .get(STORAGE0)
                                .getRepositories()
                                .get(REPOSITORY_SNAPSHOTS)
                                .isSecured())
                .as("Repository should have required authentication!")
                .isTrue();

        Repository repositoryReleases = configuration.getStorages()
                                                     .get(STORAGE0)
                                                     .getRepositories()
                                                     .get(REPOSITORY_RELEASES);

        assertThat(repositoryReleases.allowsDirectoryBrowsing()).isTrue();

        Set<String> versionValidators = repositoryReleases.getArtifactCoordinateValidators();

        assertThat(versionValidators).isNotEmpty();
        assertThat(versionValidators.contains(RedeploymentValidator.ALIAS)).isTrue();
        assertThat(versionValidators.contains(MavenSnapshotVersionValidator.ALIAS)).isTrue();
        assertThat(versionValidators.contains(MavenReleaseVersionValidator.ALIAS)).isTrue();
    }

}
