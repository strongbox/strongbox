package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactGroupEntry;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AbstractLayoutProviderTest
{

    private static final String STORAGE0 = "storage0";

    private static final String REPOSITORY_ID = "releases";

    @Inject
    private ApplicationContext ctx;

    @Inject
    private PropertiesBooter propertiesBooter;

    @SuppressWarnings({ "PMD.UnusedPrivateField",
                        "PMD.SingularField" })
    @Spy
    private RepositoryArtifactIdGroupService artifactGroupService;

    @InjectMocks
    private AbstractLayoutProvider layoutProvider = Mockito.spy(AbstractLayoutProvider.class);

    private AbstractArtifactCoordinates artifactCoordinates = Mockito.spy(AbstractArtifactCoordinates.class);

    private StorageFileSystemProviderTest storageFileSystemProvider = Mockito.spy(
            new StorageFileSystemProviderTest(FileSystems.getDefault().provider()));

    private LayoutFileSystem repositoryFileSystem;

    @BeforeEach
    public void setUp(TestInfo testInfo)
            throws IOException
    {
        RepositoryDto repository = createRepositoryDto(testInfo);

        HashMap<RepositoryFileAttributeType, Object> artifactAttributes = new HashMap<>();
        artifactAttributes.put(RepositoryFileAttributeType.ARTIFACT, Boolean.TRUE);
        artifactAttributes.put(RepositoryFileAttributeType.COORDINATES, artifactCoordinates);

        repositoryFileSystem = new LayoutFileSystem(propertiesBooter, new RepositoryData(repository),
                                                    FileSystems.getDefault(), storageFileSystemProvider)
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                throw new UnsupportedOperationException();
            }
        };

        artifactGroupService = ctx.getBean(RepositoryArtifactIdGroupService.class);

        MockitoAnnotations.initMocks(this);
        Mockito.doReturn("abs-lay-prov-test").when(artifactCoordinates).getId();
        Mockito.doReturn(artifactCoordinates).when(layoutProvider).getArtifactCoordinates(any(RepositoryPath.class));
        Mockito.doReturn(artifactAttributes).when(storageFileSystemProvider).getRepositoryFileAttributes(any(RepositoryPath.class), any());
    }

    private RepositoryDto createRepositoryDto(TestInfo testInfo)
    {
        final String repositoryId = getRepositoryId(testInfo);

        StorageDto storage = new StorageDto(STORAGE0);
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setStorage(storage);

        final Path repositoryBasePath = getRepositoryBasePath(testInfo);
        repository.setBasedir(repositoryBasePath.toAbsolutePath().toString());

        return repository;
    }

    @Test
    public void shouldReturnExpectedArtifactGroups(TestInfo testInfo)
            throws IOException
    {
        final String storageId = STORAGE0;
        final String repositoryId = getRepositoryId(testInfo);
        final Path repositoryBasePath = getRepositoryBasePath(testInfo);
        RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem).resolve("org")
                                                                                          .resolve("carlspring")
                                                                                          .resolve("abs-lay-prov-test")
                                                                                          .resolve("1.8")
                                                                                          .resolve("abs-lay-prov-test-1.8.jar");

        Set<ArtifactGroupEntry> artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).isEmpty();

        RepositoryArtifactIdGroupEntry repositoryArtifactIdGroup = artifactGroupService.findOneOrCreate(storageId,
                                                                                                        repositoryId,
                                                                                                        "abs-lay-prov-test");

        artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).hasSize((1));
        assertThat(artifactGroups.iterator().next()).isEqualTo(repositoryArtifactIdGroup);
        assertThat(repositoryArtifactIdGroup).isInstanceOf(RepositoryArtifactIdGroupEntry.class);
        assertThat(repositoryArtifactIdGroup.getArtifactId()).isEqualTo(("abs-lay-prov-test"));
        assertThat(repositoryArtifactIdGroup.getRepositoryId()).isEqualTo((repositoryId));
        assertThat(repositoryArtifactIdGroup.getStorageId()).isEqualTo((storageId));
        assertThat(repositoryArtifactIdGroup.getClass()).isEqualTo((RepositoryArtifactIdGroupEntry.class));
    }

    private Path getRepositoryBasePath(TestInfo testInfo)
    {
        final String repositoryId = getRepositoryId(testInfo);
        return Paths.get(propertiesBooter.getVaultDirectory(), "storages", STORAGE0, repositoryId);
    }

    private String getRepositoryId(TestInfo testInfo)
    {
        final String methodName = getMethodName(testInfo);
        return REPOSITORY_ID + "-" + methodName;
    }

    private String getMethodName(TestInfo testInfo)
    {
        Assumptions.assumeTrue(testInfo.getTestMethod().isPresent());
        return testInfo.getTestMethod().get().getName();
    }

    private class StorageFileSystemProviderTest extends LayoutFileSystemProvider
    {
        
        public StorageFileSystemProviderTest(FileSystemProvider target)
        {
            super(target);
        }

        @Override
        protected AbstractLayoutProvider getLayoutProvider()
        {
            return layoutProvider;
        }
        
        @Override
        protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath,
                                                                                       RepositoryFileAttributeType... attributeTypes)
        {
            return null;
        }
        
    }
}
