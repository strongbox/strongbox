package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryPathTest
{

    private static final Path REPOSITORY_BASEDIR = Paths.get(ConfigurationResourceResolver.getVaultDirectory(),
                                                             "storages", "storage0", "releases");

    private MutableRepository repository;

    private RepositoryFileSystem repositoryFileSystem;

    @Before
    public void setup()
    {
        repository = new MutableRepository();
        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

        repositoryFileSystem = new RepositoryFileSystem(new Repository(repository), FileSystems.getDefault(), null)
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test(expected = RepositoryRelativePathConstructionException.class)
    public void repositoryPathShouldNotResolveStringAbsolutePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolve("/absolute");
    }

    @Test(expected = RepositoryRelativePathConstructionException.class)
    public void repositoryPathShouldNotResolvePathAbsolutePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolve(Paths.get("/absolute"));
    }

    @Test(expected = RepositoryRelativePathConstructionException.class)
    public void repositoryPathShouldNotResolveSiblingStringAbsolutePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolveSibling("/absolute");
    }

    @Test(expected = RepositoryRelativePathConstructionException.class)
    public void repositoryPathShouldNotResolveSiblingPathAbsolutePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolveSibling(Paths.get("/absolute"));
    }

    @Test
    public void repositoryPathShouldResolveStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path = path.resolve("relative");

        Assert.assertThat(path, CoreMatchers.equalTo(REPOSITORY_BASEDIR.resolve("relative")));
    }

    @Test
    public void repositoryPathShouldResolvePathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path = path.resolve(Paths.get("relative"));

        Assert.assertThat(path, CoreMatchers.equalTo(REPOSITORY_BASEDIR.resolve("relative")));
    }

    @Test(expected = PathExceededRootRepositoryPathException.class)
    public void shouldNotBePossibleToResolveSiblingStringOutsideTheRepositoryRoot()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolveSibling("relative");
    }

    @Test(expected = PathExceededRootRepositoryPathException.class)
    public void shouldNotBePossibleToResolveSiblingPathOutsideTheRepositoryRoot()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem);

        path.resolveSibling(Paths.get("relative"));
    }


    @Test
    public void repositoryPathShouldResolveSiblingStringRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling("relative");

        Assert.assertThat(path, CoreMatchers.equalTo(REPOSITORY_BASEDIR.resolve("relative")));
    }

    @Test
    public void repositoryPathShouldResolveSiblingPathRelativePaths()
    {
        RepositoryPath path = new RepositoryPath(REPOSITORY_BASEDIR, repositoryFileSystem).resolve("onePathFurther");

        path = path.resolveSibling(Paths.get("relative"));

        Assert.assertThat(path, CoreMatchers.equalTo(REPOSITORY_BASEDIR.resolve("relative")));
    }


}
