package org.carlspring.strongbox.providers.layout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

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

    @Inject
    private PropertiesBooter propertiesBooter;
    
    @Spy
    @Inject
    private ArtifactIdGroupService artifactGroupService;

    @Spy
    @Inject
    private ArtifactIdGroupRepository artifactIdGroupRepository;
    
    @InjectMocks
    private AbstractLayoutProvider layoutProvider = Mockito.spy(AbstractLayoutProvider.class);

    private LayoutArtifactCoordinatesEntity artifactCoordinates = Mockito.spy(LayoutArtifactCoordinatesEntity.class);
    
    private StorageFileSystemProviderTest storageFileSystemProvider = Mockito.spy(new StorageFileSystemProviderTest(FileSystems.getDefault().provider()));

    private static final Path REPOSITORY_BASEDIR = Paths.get("target/strongbox-vault", "storages", "storage0", "releases");

    private LayoutFileSystem repositoryFileSystem;


    @BeforeEach
    public void setUp()
            throws IOException
    {
        StorageDto storage = new StorageDto();
        storage.setId("storage0");

        RepositoryDto repository = new RepositoryDto();
        repository.setStorage(storage);
        repository.setId("releases");

        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

        HashMap<RepositoryFileAttributeType, Object> artifactAttributes = new HashMap<>();
        artifactAttributes.put(RepositoryFileAttributeType.ARTIFACT, Boolean.TRUE);
        artifactAttributes.put(RepositoryFileAttributeType.COORDINATES, artifactCoordinates);
        
        repositoryFileSystem = new LayoutFileSystem(propertiesBooter, new RepositoryData(repository), FileSystems.getDefault(), storageFileSystemProvider)
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                throw new UnsupportedOperationException();
            }
        };

        //artifactGroupService = ctx.getBean(ArtifactIdGroupService.class);

        MockitoAnnotations.initMocks(this);
        Mockito.doReturn("abs-lay-prov-test").when(artifactCoordinates).getId();
        Mockito.doReturn(artifactCoordinates).when(layoutProvider).getArtifactCoordinates(any(RepositoryPath.class));
        Mockito.doReturn(artifactAttributes).when(storageFileSystemProvider).getRepositoryFileAttributes(any(RepositoryPath.class), any());
    }

    @Test
    public void shouldReturnExpectedArtifactGroups()
            throws IOException
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("org")
                                                                                          .resolve("carlspring")
                                                                                          .resolve("abs-lay-prov-test")
                                                                                          .resolve("1.8")
                                                                                          .resolve("abs-lay-prov-test-1.8.jar");

        Set<ArtifactGroup> artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).isEmpty();

        ArtifactIdGroup repositoryArtifactIdGroup = artifactIdGroupRepository.save(new ArtifactIdGroupEntity("storage0",
                                                                                   "releases",
                                                                                   "abs-lay-prov-test"));

        artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).hasSize((1));
        assertThat(artifactGroups.iterator().next()).isEqualTo(repositoryArtifactIdGroup);
        assertThat(repositoryArtifactIdGroup).isInstanceOf(ArtifactIdGroupEntity.class);
        assertThat(repositoryArtifactIdGroup.getArtifactId()).isEqualTo(("abs-lay-prov-test"));
        assertThat(repositoryArtifactIdGroup.getRepositoryId()).isEqualTo(("releases"));
        assertThat(repositoryArtifactIdGroup.getStorageId()).isEqualTo(("storage0"));
        assertThat(repositoryArtifactIdGroup.getClass()).isEqualTo((ArtifactIdGroupEntity.class));
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
