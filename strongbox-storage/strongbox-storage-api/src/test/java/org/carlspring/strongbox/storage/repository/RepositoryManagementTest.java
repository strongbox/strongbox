package org.carlspring.strongbox.storage.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementApplicationContext;
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

    private static Set<Repository> resolvedRepositoryInstances = ConcurrentHashMap.newKeySet();

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected PropertiesBooter propertiesBooter;

    @AfterEach
    public void checkRepositoryContextCleanup()
    {
        assertNull(TestRepositoryManagementApplicationContext.getInstance());
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @RepeatedTest(20)
    public void testRepositoryDirect(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r1", storage = "storage0") Repository r1,
                                     @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r2", storage = "storage0") Repository r2,
                                     TestInfo testInfo)
    {
        parametersShouldBeCorrectlyResolvedAndUnique(r1, r2, testInfo);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @RepeatedTest(20)
    public void testRepositoryReverse(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r2", storage = "storage0") Repository r2,
                                      @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repository = "r1", storage = "storage0") Repository r1,
                                      TestInfo testInfo)
    {
        parametersShouldBeCorrectlyResolvedAndUnique(r1, r2, testInfo);
    }

    private void parametersShouldBeCorrectlyResolvedAndUnique(Repository r1,
                                                              Repository r2,
                                                              TestInfo testInfo)
    {
        // Check that other ParameterResolvers works
        assertNotNull(testInfo);
        // Check that @TestRepository resolved
        assertNotNull(r1);
        assertNotNull(r2);
        // Check that repositories correctly resolved
        assertNotNull(configurationManager.getRepository("storage0", "r1"));
        assertNotNull(configurationManager.getRepository("storage0", "r2"));
        
        // Check that paths created
        RootRepositoryPath p1 = repositoryPathResolver.resolve(r1);
        assertTrue(Files.exists(p1));
        RootRepositoryPath p2 = repositoryPathResolver.resolve(r2);
        assertTrue(Files.exists(p2));

        assertTrue(resolvedRepositoryInstances.add(r1));
        assertTrue(resolvedRepositoryInstances.add(r2));
    }
}
