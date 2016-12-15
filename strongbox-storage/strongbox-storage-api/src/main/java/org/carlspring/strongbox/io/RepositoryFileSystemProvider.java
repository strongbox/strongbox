package org.carlspring.strongbox.io;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * This implementation wraps target {@link FileSystemProvider}. <br>
 * Note that almost all {@link Path} operations in this implementation must delegate method invocations into
 * {@link Files} utility class with {@link RepositoryPath}'s target as parameters.
 * 
 * @author Sergey Bespalov
 *
 */
public class RepositoryFileSystemProvider extends FileSystemProvider
{

    private FileSystemProvider storageFileSystemProvider;

    public RepositoryFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super();
        this.storageFileSystemProvider = storageFileSystemProvider;
    }

    public String getScheme()
    {
        return storageFileSystemProvider.getScheme();
    }

    public FileSystem newFileSystem(URI uri,
                                    Map<String, ?> env)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public FileSystem getFileSystem(URI uri)
    {
        throw new UnsupportedOperationException();
    }

    public Path getPath(URI uri)
    {
        throw new UnsupportedOperationException();
    }

    public SeekableByteChannel newByteChannel(Path path,
                                              Set<? extends OpenOption> options,
                                              FileAttribute<?>... attrs)
        throws IOException
    {
        return storageFileSystemProvider.newByteChannel(path, options, attrs);
    }

    public DirectoryStream<Path> newDirectoryStream(Path dir,
                                                    Filter<? super Path> filter)
        throws IOException
    {
        return storageFileSystemProvider.newDirectoryStream(dir, filter);
    }

    public void createDirectory(Path dir,
                                FileAttribute<?>... attrs)
        throws IOException
    {
        storageFileSystemProvider.createDirectory(dir, attrs);
    }

    public void delete(Path path)
        throws IOException
    {
        if (!(path instanceof RepositoryPath))
        {
            storageFileSystemProvider.delete(path);
            return;
        }

        RepositoryPath repositoryPath = (RepositoryPath) path;
        if (!Files.exists(repositoryPath.getTarget()))
        {
            return;
        }

        Repository repository = repositoryPath.getFileSystem().getRepository();
        if (!repository.isTrashEnabled())
        {
            Files.deleteIfExists(repositoryPath.getTarget());
            return;
        }
        RepositoryPath trashPath = calculateTrashPath(repositoryPath);

        Files.move(repositoryPath.getTarget(),
                   trashPath.getTarget(),
                   StandardCopyOption.REPLACE_EXISTING);
    }

    public void restoreTrash(RepositoryPath path)
        throws IOException
    {
        Repository repository = path.getFileSystem().getRepository();
        if (!repository.isTrashEnabled())
        {
            return;
        }

        RepositoryPath trashPath = calculateTrashPath(path);
        if (Files.exists(trashPath.getTarget()))
        {
            Files.move(trashPath.getTarget(), path.getTarget(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void deleteTrash(RepositoryPath path)
        throws IOException
    {
        Repository repository = path.getFileSystem().getRepository();
        if (!repository.isTrashEnabled())
        {
            return;
        }

        RepositoryPath trashPath = calculateTrashPath(path);
        Files.deleteIfExists(trashPath.getTarget());
    }

    private RepositoryPath calculateTrashPath(RepositoryPath repositoryPath)
    {
        RepositoryPath trashPath = repositoryPath.getFileSystem().getTrashPath();

        FileSystem sourceFileSystem = repositoryPath.getTarget().getFileSystem();
        FileSystem targetFileSystem = trashPath.getTarget().getFileSystem();

        String sourceRelative = repositoryPath.getTarget()
                                              .getRoot()
                                              .relativize(repositoryPath.getTarget())
                                              .toString();
        // XXX: We try to convert path form source to target FileSystem, and needs to be checked on different Storages.
        RepositoryPath trashArtifactPath = trashPath.resolve(sourceRelative.replaceAll(sourceFileSystem.getSeparator(),
                                                                                       targetFileSystem.getSeparator()));
        return trashArtifactPath;
    }

    public void copy(Path source,
                     Path target,
                     CopyOption... options)
        throws IOException
    {
        storageFileSystemProvider.copy(source, target, options);
    }

    public void move(Path source,
                     Path target,
                     CopyOption... options)
        throws IOException
    {
        storageFileSystemProvider.move(source, target, options);
    }

    public boolean isSameFile(Path path,
                              Path path2)
        throws IOException
    {
        return storageFileSystemProvider.isSameFile(path, path2);
    }

    public boolean isHidden(Path path)
        throws IOException
    {
        return storageFileSystemProvider.isHidden(path);
    }

    public FileStore getFileStore(Path path)
        throws IOException
    {
        return storageFileSystemProvider.getFileStore(path);
    }

    public void checkAccess(Path path,
                            AccessMode... modes)
        throws IOException
    {
        Path targetPath = path instanceof RepositoryPath ? ((RepositoryPath)path).getTarget() : path;
        storageFileSystemProvider.checkAccess(targetPath, modes);
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                Class<V> type,
                                                                LinkOption... options)
    {
        return storageFileSystemProvider.getFileAttributeView(path, type, options);
    }

    public <A extends BasicFileAttributes> A readAttributes(Path path,
                                                            Class<A> type,
                                                            LinkOption... options)
        throws IOException
    {
        return storageFileSystemProvider.readAttributes(path, type, options);
    }

    public Map<String, Object> readAttributes(Path path,
                                              String attributes,
                                              LinkOption... options)
        throws IOException
    {
        return storageFileSystemProvider.readAttributes(path, attributes, options);
    }

    public void setAttribute(Path path,
                             String attribute,
                             Object value,
                             LinkOption... options)
        throws IOException
    {
        storageFileSystemProvider.setAttribute(path, attribute, value, options);
    }

}
