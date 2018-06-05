package org.carlspring.strongbox.providers.io;

import java.nio.file.FileSystem;
import java.nio.file.PathMatcher;
import java.util.Set;

import org.carlspring.strongbox.io.FileSystemWrapper;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

/**
 * {@link RepositoryFileSystem} is a wrapper under concrete Storage {@link FileSystem}. <br>
 * The {@link RepositoryFileSystem} root is the {@link MutableRepository}'s base directory.
 *
 * @author Sergey Bespalov
 */
public abstract class RepositoryFileSystem
        extends FileSystemWrapper
{

    public static final String TRASH = ".trash";
    public static final String TEMP = ".temp";
    public static final String INDEX = ".index";
    
    private Repository repository;
    private RepositoryFileSystemProvider provider;

    public RepositoryFileSystem(Repository repository,
                                FileSystem storageFileSystem,
                                RepositoryFileSystemProvider provider)
    {
        super(storageFileSystem);
        this.repository = repository;
        this.provider = provider;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public RepositoryFileSystemProvider provider() {
        return provider;
    }

    public RootRepositoryPath getRootDirectory()
    {
        return new RootRepositoryPath(getTarget().getPath(repository.getBasedir()).toAbsolutePath().normalize(), this);
    }

    public RepositoryPath getTrashPath()
    {
        return getRootDirectory().resolve(TRASH).toAbsolutePath();
    }

    protected RepositoryPath getTempPath()
    {
        return getRootDirectory().resolve(TEMP).toAbsolutePath();
    }

    public RepositoryPath getPath(String first,
                                  String... more)
    {
        return new RepositoryPath(getTarget().getPath(first, more), this);
    }

    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        return getTarget().getPathMatcher(syntaxAndPattern);
    }
    
    public abstract Set<String> getDigestAlgorithmSet();

}
