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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        artifactGroupService = ctx.getBean(RepositoryArtifactIdGroupService.class);

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

        Set<ArtifactGroupEntry> artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).isEmpty();

        RepositoryArtifactIdGroupEntry repositoryArtifactIdGroup = artifactGroupService.findOneOrCreate("storage0",
                                                                                                        "releases",
                                                                                                        "abs-lay-prov-test");

        artifactGroups = layoutProvider.getArtifactGroups(path);
        assertThat(artifactGroups).isNotNull();
        assertThat(artifactGroups).hasSize((1));
        assertThat(artifactGroups.iterator().next()).isEqualTo(repositoryArtifactIdGroup);
        assertThat(repositoryArtifactIdGroup).isInstanceOf(RepositoryArtifactIdGroupEntry.class);
        assertThat(repositoryArtifactIdGroup.getArtifactId()).isEqualTo(("abs-lay-prov-test"));
        assertThat(repositoryArtifactIdGroup.getRepositoryId()).isEqualTo(("releases"));
        assertThat(repositoryArtifactIdGroup.getStorageId()).isEqualTo(("storage0"));
        assertThat(repositoryArtifactIdGroup.getClass()).isEqualTo((RepositoryArtifactIdGroupEntry.class));
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
