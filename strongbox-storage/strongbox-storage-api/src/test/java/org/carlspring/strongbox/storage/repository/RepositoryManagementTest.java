package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NullArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementApplicationContext;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { StorageApiTestConfig.class })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RepositoryManagementTest
{

    private static Set<RepositoryData> resolvedRepositoryInstances = ConcurrentHashMap.newKeySet();
    private static Set<byte[]> resolvedArtifactChecksums = ConcurrentHashMap.newKeySet();

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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @RepeatedTest(10)
    public void testRepositoryDirect(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repositoryId = "rmt1") RepositoryData r1,
                                     @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repositoryId = "rmt2") RepositoryData r2,
                                     @TestArtifact(resource = "artifact1.ext", generator = NullArtifactGenerator.class) Path standaloneArtifact,
                                     @TestArtifact(repositoryId = "rmt2", resource = "org/carlspring/test/artifact2.ext", generator = NullArtifactGenerator.class) Path repositoryArtifact,
                                     TestInfo testInfo)
        throws IOException
    {
        artifactShouldBeCorrectlyResolvedAndUnique(standaloneArtifact);
        artifactShouldBeCorrectlyResolvedAndUnique(repositoryArtifact);
        parametersShouldBeCorrectlyResolvedAndUnique(r1, r2, testInfo);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @RepeatedTest(10)
    public void testRepositoryReverse(@TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repositoryId = "rmt2") RepositoryData r2,
                                      @TestRepository(layout = NullArtifactCoordinates.LAYOUT_NAME, repositoryId = "rmt1") RepositoryData r1,
                                      TestInfo testInfo)
    {
        parametersShouldBeCorrectlyResolvedAndUnique(r1, r2, testInfo);
    }

    private void artifactShouldBeCorrectlyResolvedAndUnique(Path artifact)
        throws IOException
    {
        assertTrue(Files.exists(artifact));
        assertEquals(1024L, Files.size(artifact));

        String fileName = artifact.getFileName().toString();
        String checksumFileName = fileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path artifactChecksum = artifact.resolveSibling(checksumFileName);

        assertTrue(Files.exists(artifactChecksum));
        assertTrue(resolvedArtifactChecksums.add(Files.readAllBytes(artifactChecksum)));
    }

    private void parametersShouldBeCorrectlyResolvedAndUnique(RepositoryData r1,
                                                              RepositoryData r2,
                                                              TestInfo testInfo)
    {
        // Check that other ParameterResolvers works
        assertNotNull(testInfo);
        // Check that @TestRepository resolved
        assertNotNull(r1);
        assertNotNull(r2);
        // Check that repositories correctly resolved
        assertNotNull(configurationManager.getRepository(r1.getStorage().getId(), r1.getId()));
        assertNotNull(configurationManager.getRepository(r2.getStorage().getId(), r2.getId()));

        // Check that paths created
        RootRepositoryPath p1 = repositoryPathResolver.resolve(r1);
        assertTrue(Files.exists(p1));
        RootRepositoryPath p2 = repositoryPathResolver.resolve(r2);
        assertTrue(Files.exists(p2));

        assertTrue(resolvedRepositoryInstances.add(r1));
        assertTrue(resolvedRepositoryInstances.add(r2));
    }

}
