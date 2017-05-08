package org.carlspring.strongbox.providers.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

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

    protected Path getTarget()
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
        return createByTemplate(getTarget().getParent());
    }

    public int getNameCount()
    {
        return getTarget().getNameCount();
    }

    public Path getName(int index)
    {
        return getTarget().getName(index);
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
        return createByTemplate(getTarget().normalize());
    }

    public RepositoryPath resolve(Path other)
    {
        other = getTarget(other);
        return createByTemplate(getTarget().resolve(other));
    }

    public RepositoryPath resolve(String other)
    {
        return createByTemplate(getTarget().resolve(other));
    }

    public RepositoryPath resolveSibling(Path other)
    {
        other = getTarget(other);
        return createByTemplate(getTarget().resolveSibling(other));
    }

    protected Path getTarget(Path other)
    {
        other = other instanceof RepositoryPath ? ((RepositoryPath)other).getTarget() : other;
        return other;
    }

    public RepositoryPath resolveSibling(String other)
    {
        return createByTemplate(getTarget().resolveSibling(other));
    }

    public Path relativize(Path other)
    {
        other = getTarget(other);
        return getTarget().relativize(other);
    }
    
    public RepositoryPath getRepositoryRelative()
    {
        //TODO: there can be issues under Windows with replaceAll(..)
        String resultString = toString().replaceAll(getFileSystem().getRootDirectory().toString(), "");
        resultString = resultString.startsWith(getFileSystem().getSeparator()) ? resultString.substring(1) : resultString;
        return getFileSystem().getPath(resultString);
    }

    public URI toUri()
    {
        throw new UnsupportedOperationException();
    }

    public RepositoryPath toAbsolutePath()
    {
        return createByTemplate(getTarget().toAbsolutePath());
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
        return getTarget().compareTo(other);
    }

    public RepositoryPath createByTemplate(Path path)
    {
        
        RepositoryPath repositoryPath = new RepositoryPath(path, fileSystem);
        if (!Files.isDirectory(path) && getFileSystem().isArtifact(repositoryPath.getRepositoryRelative().toString()))
        {
            return repositoryPath.toArtifactPath();
        }
        return repositoryPath;
    }

    public ArtifactPath toArtifactPath()
    {
        ArtifactCoordinates coordinates = getFileSystem().getArtifactCoordinates(this);
        return new ArtifactPath(coordinates, target, fileSystem);
    }
    
    
    public boolean isMetadata(){
        return getFileSystem().isMetadata(getRepositoryRelative().toString());
    }
    
    public String toString()
    {
        return target.toString();
    }

}
