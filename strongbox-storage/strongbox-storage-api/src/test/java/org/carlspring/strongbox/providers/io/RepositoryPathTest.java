package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import javax.inject.Inject;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class RepositoryPathTest
{

    private static final String STORAGE_ID = "storage0";

    private static final String REPOSITORY_ID = "releases";

    private Path repositoryBasePath;

    private RepositoryDto repository;

    private LayoutFileSystem repositoryFileSystem;

    @Inject
    private PropertiesBooter propertiesBooter;

    @BeforeEach
    public void setup()
    {
        repositoryBasePath = getRepositoryBasePath();

        repository = new RepositoryDto();
        repository.setBasedir(repositoryBasePath.toAbsolutePath().toString());

        PropertiesBooter propertiesBooter = new PropertiesBooter();
        repositoryFileSystem = new LayoutFileSystem(propertiesBooter, new RepositoryData(repository), FileSystems.getDefault(), null)
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void repositoryPathShouldNotResolveStringAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                    path.resolve("/absolute");
        });
    }

    @Test
    public void repositoryPathShouldNotResolvePathAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                    path.resolve(Paths.get("/absolute"));
        });
    }

    @Test
    public void repositoryPathShouldNotResolveSiblingStringAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                    path.resolveSibling("/absolute");
        });
    }

    @Test
    public void repositoryPathShouldNotResolveSiblingPathAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                    path.resolveSibling(Paths.get("/absolute"));
        });
    }

    @Test
    public void repositoryPathShouldResolveStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);

        path = path.resolve("relative");

        assertThat(path).isEqualTo(repositoryBasePath.resolve("relative"));
    }

    @Test
    public void repositoryPathShouldResolvePathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);

        path = path.resolve(Paths.get("relative"));

        assertThat(path).isEqualTo(repositoryBasePath.resolve("relative"));
    }

    @Test
    public void shouldNotBePossibleToResolveSiblingStringOutsideTheRepositoryRoot()
    {
        assertThatExceptionOfType(PathExceededRootRepositoryPathException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                    path.resolveSibling("relative");
        });
    }

    @Test
    public void shouldNotBePossibleToResolveSiblingPathOutsideTheRepositoryRoot()
    {
        assertThatExceptionOfType(PathExceededRootRepositoryPathException.class)
             .isThrownBy(() -> {
                 RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem);
                 path.resolveSibling(Paths.get("relative"));
        });
    }


    @Test
    public void repositoryPathShouldResolveSiblingStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling("relative");

        assertThat(path).isEqualTo(repositoryBasePath.resolve("relative"));
    }

    @Test
    public void repositoryPathShouldResolveSiblingPathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(repositoryBasePath, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling(Paths.get("relative"));

        assertThat(path).isEqualTo(repositoryBasePath.resolve("relative"));
    }

    private Path getRepositoryBasePath()
    {
        return Paths.get(propertiesBooter.getVaultDirectory(), "storages", STORAGE_ID, REPOSITORY_ID);
    }
}
