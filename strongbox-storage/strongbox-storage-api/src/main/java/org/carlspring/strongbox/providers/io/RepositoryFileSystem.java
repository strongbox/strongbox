package org.carlspring.strongbox.providers.io;

import java.nio.file.FileSystem;
import java.nio.file.PathMatcher;
import java.nio.file.spi.FileSystemProvider;

import org.carlspring.strongbox.storage.repository.Repository;

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
        return this.getPath(repository.getBasedir());
    }

    public RepositoryPath getTrashPath()
    {
        return getRootDirectory().resolve(".trash");
    }

    public RepositoryPath getTempPath()
    {
        return getRootDirectory().resolve(".temp");
    }

    public RepositoryPath getPath(String first,
                                  String... more)
    {
        return new RepositoryPath(getTarget().getPath(first, more), this);
    }

    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        throw new UnsupportedOperationException();
    }

}
