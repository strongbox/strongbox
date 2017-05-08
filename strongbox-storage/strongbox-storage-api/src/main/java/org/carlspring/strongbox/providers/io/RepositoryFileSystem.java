package org.carlspring.strongbox.providers.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.FileSystemWrapper;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * {@link RepositoryFileSystem} is a wrapper under concrete Storage {@link FileSystem}. <br>
 * The {@link RepositoryFileSystem} root is the {@link Repository}'s base directory.
 *
 * @author Sergey Bespalov
 */
public abstract class RepositoryFileSystem
        extends FileSystemWrapper
{

    private Repository repository;
    private RepositoryFileSystemProvider fileSystemProvider;

    public RepositoryFileSystem(Repository repository,
                                FileSystem storageFileSystem,
                                RepositoryFileSystemProvider provider)
    {
        super(storageFileSystem);
        this.repository = repository;
        this.fileSystemProvider = provider;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public RepositoryFileSystemProvider provider()
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
    
    public abstract Set<String> getDigestAlgorithmSet();
    
    public abstract boolean isMetadata(String path);
    
    protected boolean isChecksum(String path)
    {
        for (String e : getDigestAlgorithmSet())
        {
            if (path.endsWith("." + e.replaceAll("-", "").toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isTrash(String path)
    {
        return path.startsWith(".trash");
    }

    protected boolean isTemp(String path)
    {
        return path.startsWith(".temp");
    }

    protected boolean isIndex(String path)
    {
        return path.startsWith(".index");
    }

    protected boolean isArtifact(String path)
    {
        return !isMetadata(path) && !isChecksum(path) && !isServiceFolder(path);
    }

    protected boolean isServiceFolder(String path)
    {
        return isTemp(path) || isTrash(path) || isIndex(path);
    }

    public abstract ArtifactCoordinates getArtifactCoordinates(RepositoryPath path);
}
