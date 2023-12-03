package org.carlspring.strongbox.providers.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public class RepositoryPathTest
{

    private static final Path REPOSITORY_BASEDIR = Paths.get("target/strongbox-vault/storages/storage0/releases").toAbsolutePath();

    private RepositoryDto repository;

    private LayoutFileSystem repositoryFileSystem;


    @BeforeEach
    public void setup()
    {
        repository = new RepositoryDto();
        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

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
                    RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                    path.resolve("/absolute");
        });
    }

    @Test
    public void repositoryPathShouldNotResolvePathAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                    path.resolve(Paths.get("/absolute"));
        });
    }

    @Test
    public void repositoryPathShouldNotResolveSiblingStringAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                    path.resolveSibling("/absolute");
        });
    }

    @Test
    public void repositoryPathShouldNotResolveSiblingPathAbsolutePaths()
    {
        assertThatExceptionOfType(RepositoryRelativePathConstructionException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                    path.resolveSibling(Paths.get("/absolute"));
        });
    }

    @Test
    public void repositoryPathShouldResolveStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path = path.resolve("relative");

        assertThat(path).isEqualTo(REPOSITORY_BASEDIR.resolve("relative"));
    }

    @Test
    public void repositoryPathShouldResolvePathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path = path.resolve(Paths.get("relative"));

        assertThat(path).isEqualTo(REPOSITORY_BASEDIR.resolve("relative"));
    }

    @Test
    public void shouldNotBePossibleToResolveSiblingStringOutsideTheRepositoryRoot()
    {

        assertThatExceptionOfType(PathExceededRootRepositoryPathException.class)
                .isThrownBy(() -> {
                    RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                    path.resolveSibling("relative");
        });
    }

    @Test
    public void shouldNotBePossibleToResolveSiblingPathOutsideTheRepositoryRoot()
    {
        assertThatExceptionOfType(PathExceededRootRepositoryPathException.class)
             .isThrownBy(() -> {
                 RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);
                 path.resolveSibling(Paths.get("relative"));
        });
    }


    @Test
    public void repositoryPathShouldResolveSiblingStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling("relative");

        assertThat(path).isEqualTo(REPOSITORY_BASEDIR.resolve("relative"));
    }

    @Test
    public void repositoryPathShouldResolveSiblingPathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling(Paths.get("relative"));

        assertThat(path).isEqualTo(REPOSITORY_BASEDIR.resolve("relative"));
    }


}
