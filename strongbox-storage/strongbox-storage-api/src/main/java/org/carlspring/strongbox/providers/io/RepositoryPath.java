package org.carlspring.strongbox.providers.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.regex.Pattern;

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
        
        return wrap(getTarget().resolve(other));
    }

    public RepositoryPath resolve(String other)
    {
        return wrap(getTarget().resolve(other));
    }

    public RepositoryPath resolveSibling(Path other)
    {
        other = unwrap(other);
        
        return wrap(getTarget().resolveSibling(other));
    }

    protected Path unwrap(Path other)
    {
        other = other instanceof RepositoryPath ? ((RepositoryPath)other).getTarget() : other;
        
        return other;
    }

    public RepositoryPath resolveSibling(String other)
    {
        return wrap(getTarget().resolveSibling(other));
    }

    public RepositoryPath relativize(Path other)
    {
        other = unwrap(other);
        
        return wrap(getTarget().relativize(other));
    }
    
    public RepositoryPath getRepositoryRelative()
    {
        return getFileSystem().getRootDirectory().relativize(this); 
    }

    public URI toUri()
    {
        throw new UnsupportedOperationException();
    }

    public RepositoryPath toAbsolutePath()
    {
        return wrap(getTarget().toAbsolutePath());
    }

    public Path toRealPath(LinkOption... options)
            throws IOException
    {
        throw new UnsupportedOperationException();
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
 
    public static void main(String args[]) throws Exception {
        System.out.println(Pattern.quote("z:\\strongbox\\strongbox\\strongbox-storage\\strongbox-storage-layout-providers\\strongbox-storage-maven-layout-provider\\target\\strongbox-vault\\storages\\storage0\\m2lp-releases"));
    }
}
