package org.carlspring.strongbox.storage.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { StorageApiTestConfig.class })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(ExecutionMode.CONCURRENT)
public class RepositoryManagementTest
{

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected PropertiesBooter propertiesBooter;

    @AfterEach
    public void checkRepositoryCleanup()
    {
        repositoriesShouldBeCleaned();
    }

    private void repositoriesShouldBeCleaned()
    {
        Repository r1 = configurationManager.getRepository("storage0", "r1");
        assertNull(r1);
        Repository r2 = configurationManager.getRepository("storage0", "r2");
        assertNull(r2);

        Path p1 = Paths.get(propertiesBooter.getVaultDirectory(), "storages", "storage0", "r1");
        assertFalse(Files.exists(p1));
        Path p2 = Paths.get(propertiesBooter.getVaultDirectory(), "storages", "storage0", "r2");
        assertFalse(Files.exists(p2));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @RepeatedTest(10)
    public void testRepositoryDirect(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r1", storage = "storage0") Repository r1,
                                     @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r2", storage = "storage0") Repository r2,
                                     TestInfo testInfo)
    {
        parametersShouldBeResolved(r1, r2, testInfo);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @RepeatedTest(10)
    public void testRepositoryReverse(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r2", storage = "storage0") Repository r2,
                                      @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r1", storage = "storage0") Repository r1,
                                      TestInfo testInfo)
    {
        parametersShouldBeResolved(r1, r2, testInfo);
    }

    private void parametersShouldBeResolved(Repository r1,
                                            Repository r2,
                                            TestInfo testInfo)
    {
        // Check that other ParameterResolvers works
        assertNotNull(testInfo);
        // Check that @TestRepository resolved
        assertNotNull(r1);
        assertNotNull(r2);
        // Check that paths created
        RootRepositoryPath p1 = repositoryPathResolver.resolve(r1);
        assertTrue(Files.exists(p1));
        RootRepositoryPath p2 = repositoryPathResolver.resolve(r2);
        assertTrue(Files.exists(p2));
    }
}
