package org.carlspring.strongbox.io;

import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;

/**
 * {@link RepositoryFileSystem} is a wrapper under concrete Storage {@link FileSystem}. <br>
 * The {@link RepositoryFileSystem} root is the {@link Repository}'s base directory.
 *
 * @author Sergey Bespalov
 */
public class RepositoryFileSystem
        extends FileSystemWrapper
{

    private Repository repository;
    private FileSystemProvider fileSystemProvider;

    public RepositoryFileSystem(Repository repository,
                                FileSystem storageFileSystem,
                                FileSystemProvider provider)
    {
        super(storageFileSystem);
        this.repository = repository;
        this.fileSystemProvider = provider;
    }

    public RepositoryFileSystem(Repository repository,
                                FileSystem storageFileSystem)
    {
        this(repository, storageFileSystem, new RepositoryFileSystemProvider(storageFileSystem.provider()));
    }

    public Repository getRepository()
    {
        return repository;
    }

    public FileSystemProvider provider()
    {
        return fileSystemProvider;
    }

    public RepositoryPath getRootDirectory()
    {
        Path root = Paths.get(repository.getBasedir());
        FileSystem rootFileSystem = root.getFileSystem();
        return new RepositoryPath(root,
                                  new RepositoryFileSystem(repository, rootFileSystem,
                                                           new RepositoryFileSystemProvider(rootFileSystem.provider())));
    }

    public RepositoryPath getTrashPath()
    {
        return getRootDirectory().resolve(".trash");
    }

    public RepositoryPath getTempPath()
    {
        return getRootDirectory().resolve(".temp");
    }

    public Path getPath(String first,
                        String... more)
    {
        return new RepositoryPath(getTarget().getPath(first, more), this);
    }

    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        throw new UnsupportedOperationException();
    }

    public static RepositoryFileSystem getRepositoryFileSystem(Repository repository)
    {
        FileSystem storageFileSystem = new FileSystemWrapper(Paths.get(repository.getBasedir())
                                                                  .getFileSystem())
        {

            @Override
            public Path getRootDirectory()
            {
                return Paths.get(repository.getBasedir());
            }

        };
        return new RepositoryFileSystem(repository, storageFileSystem);
    }

}
