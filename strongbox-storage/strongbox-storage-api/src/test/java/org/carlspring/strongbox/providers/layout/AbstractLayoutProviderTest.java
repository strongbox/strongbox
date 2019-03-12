package org.carlspring.strongbox.providers.layout;

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
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactGroupEntry;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.StorageFileSystemProvider;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AbstractLayoutProviderTest
{

    @Inject
    private ApplicationContext ctx;

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
        MutableStorage storage = new MutableStorage();
        storage.setId("storage0");

        MutableRepository repository = new MutableRepository();
        repository.setStorage(storage);
        repository.setId("releases");

        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

        HashMap<RepositoryFileAttributeType, Object> artifactAttributes = new HashMap<>();
        artifactAttributes.put(RepositoryFileAttributeType.ARTIFACT, Boolean.TRUE);
        artifactAttributes.put(RepositoryFileAttributeType.COORDINATES, artifactCoordinates);
        
        repositoryFileSystem = new LayoutFileSystem(new ImmutableRepository(repository), FileSystems.getDefault(), storageFileSystemProvider)
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
        MatcherAssert.assertThat(artifactGroups, Matchers.notNullValue());
        MatcherAssert.assertThat(artifactGroups.size(), CoreMatchers.equalTo(0));
        
        ArtifactGroupEntry artifactGroup = artifactGroupService.findOneOrCreate("storage0", "releases", "abs-lay-prov-test");
        
        artifactGroups = layoutProvider.getArtifactGroups(path);
        MatcherAssert.assertThat(artifactGroups, Matchers.notNullValue());
        MatcherAssert.assertThat(artifactGroups.size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(artifactGroups.iterator().next(), Matchers.equalTo(artifactGroup));
        MatcherAssert.assertThat(artifactGroup, CoreMatchers.instanceOf(RepositoryArtifactIdGroupEntry.class));
        RepositoryArtifactIdGroupEntry repositoryArtifactIdGroup = (RepositoryArtifactIdGroupEntry) artifactGroup;
        MatcherAssert.assertThat(repositoryArtifactIdGroup.getArtifactId(), CoreMatchers.equalTo("abs-lay-prov-test"));
        MatcherAssert.assertThat(repositoryArtifactIdGroup.getRepositoryId(), CoreMatchers.equalTo("releases"));
        MatcherAssert.assertThat(repositoryArtifactIdGroup.getStorageId(), CoreMatchers.equalTo("storage0"));
        MatcherAssert.assertThat(artifactGroup.getClass(), CoreMatchers.equalTo(RepositoryArtifactIdGroupEntry.class));
    }
    
    private class StorageFileSystemProviderTest extends StorageFileSystemProvider
    {

        public StorageFileSystemProviderTest(FileSystemProvider target)
        {
            super(target);
        }

        @Override
        protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath,
                                                                                       RepositoryFileAttributeType... attributeTypes)
            throws IOException
        {
            return null;
        }
        
    }
}
