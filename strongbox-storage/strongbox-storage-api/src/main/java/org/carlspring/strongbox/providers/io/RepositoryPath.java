package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.PathUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * This implementation wraps target {@link Path} implementation, which can be an "CloudPath" or common
 * "FileSystemPath".
 *
 * @author Sergey Bespalov
 */
public class RepositoryPath
        implements Path
{

    private Path target;
    private RepositoryFileSystem fileSystem;
    protected ArtifactEntry artifactEntry;

    public RepositoryPath(Path target,
                          RepositoryFileSystem fileSystem)
    {
        this.target = target;
        this.fileSystem = fileSystem;
    }

    /**
     * Only for internal usage. Don't use this method if you not strongly sure that you need it.
     * 
     * @return
     */
    public Path getTarget()
    {
        return target;
    }
    
    public ArtifactEntry getArtifactEntry()
    {
        return artifactEntry;
    }

    public RepositoryFileSystem getFileSystem()
    {
        return fileSystem;
    }

    public boolean isAbsolute()
    {
        return getTarget().isAbsolute();
    }

    public RepositoryPath getRoot()
    {
        return (RepositoryPath) fileSystem.getRootDirectories()
                                          .iterator()
                                          .next();
    }

    public Path getFileName()
    {
        return getTarget().getFileName();
    }

    public RepositoryPath getParent()
    {
        return wrap(getTarget().getParent());
    }

    public int getNameCount()
    {
        return getTarget().getNameCount();
    }

    public RepositoryPath getName(int index)
    {
        return wrap(getTarget().getName(index));
    }

    public Path subpath(int beginIndex,
                        int endIndex)
    {
        throw new UnsupportedOperationException();
    }

    public boolean startsWith(Path other)
    {
        return getTarget().startsWith(other);
    }

    public boolean startsWith(String other)
    {
        return getTarget().startsWith(other);
    }

    public boolean endsWith(Path other)
    {
        return getTarget().endsWith(other);
    }

    public boolean endsWith(String other)
    {
        return getTarget().endsWith(other);
    }

    public RepositoryPath normalize()
    {
        return wrap(getTarget().normalize());
    }

    public RepositoryPath resolve(Path other)
    {
        if (other == null)
        {
            return this;
        }

        other = unwrap(other);

        validatePathRelativized(other);
        
        return wrap(getTarget().resolve(other));
    }

    public RepositoryPath resolve(String other)
    {
        if (other == null)
        {
            return this;
        }

        validateStringPathRelativized(other);

        return wrap(getTarget().resolve(other));
    }

    public RepositoryPath resolveSibling(Path other)
    {
        validatePathRelativized(other);

        other = unwrap(other);

        RepositoryPath result = wrap(getTarget().resolveSibling(other));

        validateSibling(result);

        return result;
    }

    protected Path unwrap(Path other)
    {
        other = other instanceof RepositoryPath ? ((RepositoryPath)other).getTarget() : other;
        
        return other;
    }

    public RepositoryPath resolveSibling(String other)
    {
        validateStringPathRelativized(other);

        RepositoryPath result = wrap(getTarget().resolveSibling(other));

        validateSibling(result);

        return result;
    }

    public RepositoryPath relativize(Path other)
    {
        other = unwrap(other);
        
        return wrap(getTarget().relativize(other));
    }
    
    /**
     * Returns Path relative to Repository root.
     * 
     * @return
     */
    public RepositoryPath relativize()
    {
        if (!isAbsolute())
        {
            return this;
        }
        return getFileSystem().getRootDirectory().relativize(this);
    }

    public URI toUri()
    {
        if (!isAbsolute())
        {
            return getTarget().toUri();
        }
        
        Repository repository = getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        URI result = null;
        try
        {
            result = new URI(RepositoryFileSystemProvider.STRONGBOX_SCHEME, null,
                    String.format("/%s/%s/", storage.getId(), repository.getId()), null);
        }
        catch (URISyntaxException e)
        {
            return null;
        }
        
        URI pathToken = getFileSystem().getRootDirectory().getTarget().toUri().relativize(getTarget().toUri()); 
        
        return result.resolve(pathToken);
    }
    
    public RepositoryPath toAbsolutePath()
    {
        return wrap(getTarget().toAbsolutePath());
    }

    public Path toRealPath(LinkOption... options)
            throws IOException
    {
        return getTarget().toRealPath(options);
    }

    public File toFile()
    {
        return getTarget().toFile();
    }

    public WatchKey register(WatchService watcher,
                             Kind<?>[] events,
                             Modifier... modifiers)
            throws IOException
    {
        return getTarget().register(watcher, events, modifiers);
    }

    public WatchKey register(WatchService watcher,
                             Kind<?>... events)
            throws IOException
    {
        return getTarget().register(watcher, events);
    }

    public Iterator<Path> iterator()
    {
        return getTarget().iterator();
    }

    public int compareTo(Path other)
    {
        return getTarget().compareTo(unwrap(other));
    }

    public RepositoryPath wrap(Path path)
    {
        return new RepositoryPath(path, fileSystem);
    }
    
    public String toString()
    {
        return target.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return  getTarget().equals(obj instanceof RepositoryPath ? unwrap((Path) obj) : obj);
    }

    @Override
    public int hashCode()
    {
        return getTarget().hashCode();
    }

    private void validatePathRelativized(final Path other)
    {
        if (!PathUtils.isRelativized(target, other))
        {
            throw new RepositoryRelativePathConstructionException();
        }
    }

    private void validateStringPathRelativized(final String other)
    {
        if (!PathUtils.isRelativized(target, other))
        {
            throw new RepositoryRelativePathConstructionException();
        }
    }

    private void validateSibling(final Path result)
    {
        final Path sibling = result;
        final String repositoryRootPath = getRoot().toString(); // String, intentionally
        if (!sibling.startsWith(repositoryRootPath))
        {
            throw new PathExceededRootRepositoryPathException();
        }
    }

}
