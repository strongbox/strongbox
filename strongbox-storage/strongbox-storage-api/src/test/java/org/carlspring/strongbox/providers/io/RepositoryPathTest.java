package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public class RepositoryPathTest
{

    private Path repositoryBasePath;

    private RepositoryDto repository;

    private LayoutFileSystem repositoryFileSystem;

    @BeforeEach
    public void setup()
            throws FileNotFoundException,
                   URISyntaxException
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
            throws FileNotFoundException,
                   URISyntaxException
    {
        ClassLoader classLoader = getClass().getClassLoader();

        // Directory "target/test-classes" is loaded
        URL resource = classLoader.getResource("");
        if (resource != null)
        {
            return Paths.get(resource.toURI());
        }

        throw new FileNotFoundException("Resource 'target/test-classes' was not found");
    }
}
