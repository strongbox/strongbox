package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.RawArtifactGenerator;
import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class RawLayoutProviderTest
{

    private final Logger logger = LoggerFactory.getLogger(RawLayoutProviderTest.class);

    private static final String STORAGE = "rlpt-storage-raw";

    private static final String REPOSITORY = "rlpt-raw-releases";

    @Inject
    private ArtifactManagementService artifactManagementService;
    
    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Test
    void testNullArtifactCoordinates()
    {
        RawArtifactCoordinates coordinates = new RawArtifactCoordinates("foo/bar/blah.bz2");

        logger.debug("coordinates.toPath(): {}", coordinates.buildPath());
    }

    @ExtendWith({RepositoryManagementTestExecutionListener.class,
                 ArtifactManagementTestExecutionListener.class })
    @Test
    void testDeployAndResolveArtifact(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                      storageId = STORAGE,
                                                      repositoryId = REPOSITORY)
                                     Repository repository,
                                     @TestArtifact(resource = "foo/bar.zip",
                                                   generator = RawArtifactGenerator.class)
                                     Path artifactPath)
            throws Exception
    {
        String path = "foo/bar.zip";

        RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(repository, path);

        // Deploy the artifact
        artifactManagementService.validateAndStore(artifactRepositoryPath,
                                                   Files.newInputStream(artifactPath));

        assertThat(Files.exists(artifactRepositoryPath)).as("Failed to deploy artifact!").isTrue();
        assertThat(Files.size(artifactRepositoryPath) > 0).as("Failed to deploy artifact!").isTrue();

        // Attempt to re-deploy the artifact
        try
        {
            artifactManagementService.validateAndStore(artifactRepositoryPath,
                                                       Files.newInputStream(artifactPath));
        }
        catch (Exception e)
        {
            if (e.getMessage().contains("repository does not allow artifact re-deployment"))
            {
                // This is expected
                logger.debug("Successfully declined to re-deploy {}!", artifactRepositoryPath);
            }
            else
            {
                throw e;
            }
        }

        // Attempt to resolve the artifact
        try (InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            int total = 0;
            int len;
            final int size = 4096;
            byte[] bytes = new byte[size];

            while ((len = is.read(bytes, 0, size)) != -1)
            {
                total += len;
            }

            assertThat(total > 0).as("Failed to resolve artifact!").isTrue();
        }
    }

}
