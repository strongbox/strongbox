package org.carlspring.strongbox.providers.io;

import java.nio.file.FileSystem;
import java.util.Set;

import org.carlspring.strongbox.io.StorageFileSystem;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * This class decorates {@link StorageFileSystem} with common layout specific
 * logic. <br>
 * Root folder is the {@link Repository} base directory.
 *
 * @author Sergey Bespalov
 * 
 * @see Repository
 * @see LayoutProvider
 */
public abstract class LayoutFileSystem
        extends StorageFileSystem
{

    public static final String TRASH = ".trash";
    public static final String TEMP = ".temp";
    public static final String INDEX = ".index";

    private Repository repository;
    private StorageFileSystemProvider provider;

    public LayoutFileSystem(Repository repository,
                            FileSystem storageFileSystem,
                            StorageFileSystemProvider provider)
    {
        super(storageFileSystem);
        this.repository = repository;
        this.provider = provider;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public StorageFileSystemProvider provider()
    {
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

    public abstract Set<String> getDigestAlgorithmSet();

}
