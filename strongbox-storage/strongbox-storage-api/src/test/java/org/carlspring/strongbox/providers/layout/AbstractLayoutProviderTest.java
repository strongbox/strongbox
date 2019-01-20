package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactGroupService;
import org.carlspring.strongbox.services.impl.ArtifactGroupServiceImpl;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AbstractLayoutProviderTest
{

    @InjectMocks
    private AbstractLayoutProvider layoutProvider = Mockito.spy(AbstractLayoutProvider.class);

    private AbstractArtifactCoordinates artifactCoordinates = Mockito.spy(AbstractArtifactCoordinates.class);

    private static final Path REPOSITORY_BASEDIR = Paths.get(ConfigurationResourceResolver.getVaultDirectory(),
                                                             "storages", "storage0", "releases");

    private MutableRepository repository;

    private LayoutFileSystem repositoryFileSystem;

    @Spy
    private ArtifactGroupService artifactGroupService = new ArtifactGroupServiceImpl();

    @BeforeEach
    public void setup()
            throws IOException
    {
        repository = new MutableRepository();
        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

        repositoryFileSystem = new LayoutFileSystem(new Repository(repository), FileSystems.getDefault(), null)
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                throw new UnsupportedOperationException();
            }
        };

        MockitoAnnotations.initMocks(this);
        Mockito.doReturn("abs-lay-prov-test").when(artifactCoordinates).getId();
        Mockito.doReturn(artifactCoordinates).when(layoutProvider).getArtifactCoordinates(any(RepositoryPath.class));
    }

    @Test
    public void getArtifactGroupsTest()
            throws IOException
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("org")
                                                                                          .resolve("carlspring")
                                                                                          .resolve("abs-lay-prov-test")
                                                                                          .resolve("1.8")
                                                                                          .resolve(
                                                                                                  "abs-lay-prov-test-1.8.jar");

        Assertions.assertNotNull(layoutProvider.getArtifactGroups(path));
    }
}