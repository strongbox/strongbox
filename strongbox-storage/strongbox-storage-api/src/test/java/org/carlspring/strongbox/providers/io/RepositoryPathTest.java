package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.nio.fs.DefaultFileSystemProvider;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryPathTest
{

    private static final Path REPOSITORY_BASEDIR = Paths.get(ConfigurationResourceResolver.getVaultDirectory(),
                                                             "storages", "storage0", "releases");

    private MutableRepository repository;

    private RepositoryFileSystem repositoryFileSystem;

    private static void delete(Path directory)
            throws IOException
    {

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
        {

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc)
                    throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Before
    public void setup()
    {
        repository = new MutableRepository();
        repository.setBasedir(REPOSITORY_BASEDIR.toAbsolutePath().toString());

        repositoryFileSystem = new RepositoryFileSystem(new Repository(repository), FileSystems.getDefault(),
                                                        new RepositoryFileSystemProvider(DefaultFileSystemProvider.create())
                                                        {
                                                            @Override
                                                            protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(final RepositoryPath repositoryRelativePath,
                                                                                                                                           final RepositoryFileAttributeType... attributeTypes)
                                                                    throws IOException
                                                            {
                                                                throw new UnsupportedOperationException();
                                                            }
                                                        })
        {
            @Override
            public Set<String> getDigestAlgorithmSet()
            {
                return Collections.emptySet();
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

    @Test
    public void newPathShouldNotBeOld()
            throws Exception
    {
        Path tempDir = Files.createDirectories(Paths.get("RepositoryPathTest-tmp"));
        Path tempFile = Files.createTempFile(tempDir, "strongbox-isOld-test", ".tmp");

        RepositoryPath repositoryPath = new RepositoryPath(tempFile, repositoryFileSystem);
        Assert.assertFalse(repositoryPath.isOld());

        delete(tempDir);
    }

    @Test
    public void shouldBeOld2DaysAgo()
            throws Exception
    {
        Path tempDir = Files.createDirectories(Paths.get("RepositoryPathTest-tmp"));
        Path tempFile = Files.createTempFile(tempDir, "strongbox-isOld-test", ".tmp");

        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        FileTime ftTwoDaysAgo = FileTime.fromMillis(
                twoDaysAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Files.setLastModifiedTime(tempFile, ftTwoDaysAgo);

        RepositoryPath repositoryPath = new RepositoryPath(tempFile, repositoryFileSystem);
        Assert.assertTrue(repositoryPath.isOld());

        delete(tempDir);
    }

    @Test
    public void shouldNotBeOld23HoursAgo()
            throws Exception
    {
        Path tempDir = Files.createDirectories(Paths.get("RepositoryPathTest-tmp"));
        Path tempFile = Files.createTempFile(tempDir, "strongbox-isOld-test", ".tmp");

        LocalDateTime twentyThreeHoursAgo = LocalDateTime.now().minusHours(23);
        FileTime ftTwentyThreeHoursAgo = FileTime.fromMillis(
                twentyThreeHoursAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Files.setLastModifiedTime(tempFile, ftTwentyThreeHoursAgo);

        RepositoryPath repositoryPath = new RepositoryPath(tempFile, repositoryFileSystem);
        Assert.assertFalse(repositoryPath.isOld());

        delete(tempDir);
    }


}
