package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a proxy implementation which wraps target {@link FileSystemProvider}. <br>
 * Note that almost all {@link Path} operations in this implementation must delegate method invocations into
 * {@link Files} utility class with {@link RepositoryPath}'s target as parameters.
 * 
 * @author Sergey Bespalov
 *
 */
public class RepositoryFileSystemProvider extends FileSystemProvider
{
    private static final Logger logger = LoggerFactory.getLogger(RepositoryFileSystemProvider.class);

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
        return storageFileSystemProvider.newByteChannel(getTargetPath(path), options, attrs);
    }

    public DirectoryStream<Path> newDirectoryStream(Path dir,
                                                    Filter<? super Path> filter)
        throws IOException
    {
        return storageFileSystemProvider.newDirectoryStream(getTargetPath(dir), filter);
    }

    public void createDirectory(Path dir,
                                FileAttribute<?>... attrs)
        throws IOException
    {
        storageFileSystemProvider.createDirectory(getTargetPath(dir), attrs);
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
        RepositoryPath trashPath = getTrashPath(repositoryPath);

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

        RepositoryPath trashPath = getTrashPath(path);
        if (Files.exists(trashPath.getTarget()))
        {
            Files.move(trashPath.getTarget(), path.getTarget(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void restoreFromTemp(RepositoryPath path)
        throws IOException
    {
        RepositoryPath tempPath = getTempPath(path);
        if (Files.exists(tempPath.getTarget()))
        {
            if (!Files.exists(getTargetPath(path).getParent())){
                Files.createDirectories(getTargetPath(path).getParent());
            }
            Files.move(tempPath.getTarget(), path.getTarget(), StandardCopyOption.REPLACE_EXISTING);
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

        RepositoryPath trashPath = getTrashPath(path);
        Files.deleteIfExists(trashPath.getTarget());
    }

    public RepositoryPath getTempPath(RepositoryPath path) throws IOException
    {
        RepositoryPath tempPathBase = path.getFileSystem().getTempPath();
        RepositoryPath tempPath = rebase(path, tempPathBase);
        if (!Files.exists(tempPath.getParent().getTarget())){
            logger.debug(String.format("Creating: dir-[%s]", tempPath.getParent()));
            Files.createDirectories(tempPath.getParent().getTarget());
        }
        return tempPath;
    }

    public RepositoryPath getTrashPath(RepositoryPath path)
    {
        RepositoryPath trashPath = path.getFileSystem().getTrashPath();
        return rebase(path, trashPath);
    }

    private static RepositoryPath rebase(RepositoryPath source,
                                         RepositoryPath targetBase)
    {
        FileSystem sourceFileSystem = source.getTarget().getFileSystem();
        FileSystem targetFileSystem = targetBase.getTarget().getFileSystem();

        String sourceRelative = source.getRoot()
                                      .relativize(source.getTarget())
                                      .toString();
        // sbespalov: We try to convert path form source to target FileSystem
        // sbespalov: Need to check this on different Storage types.
        String sTargetPath = sourceRelative.replaceAll(sourceFileSystem.getSeparator(),
                                                                             targetFileSystem.getSeparator());
        RepositoryPath target = targetBase.resolve(sTargetPath);
        return target;
    }

    @Override
    public InputStream newInputStream(Path path,
                                      OpenOption... options)
        throws IOException
    {
        return super.newInputStream(getTargetPath(path), options);
    }

    @Override
    public OutputStream newOutputStream(Path path,
                                        OpenOption... options)
        throws IOException
    {
        return super.newOutputStream(getTargetPath(path), options);
    }

    public void copy(Path source,
                     Path target,
                     CopyOption... options)
        throws IOException
    {
        storageFileSystemProvider.copy(getTargetPath(source), getTargetPath(target), options);
    }

    public void move(Path source,
                     Path target,
                     CopyOption... options)
        throws IOException
    {
        storageFileSystemProvider.move(getTargetPath(source), getTargetPath(target), options);
    }

    public boolean isSameFile(Path path,
                              Path path2)
        throws IOException
    {
        return storageFileSystemProvider.isSameFile(getTargetPath(path), getTargetPath(path2));
    }

    public boolean isHidden(Path path)
        throws IOException
    {
        return storageFileSystemProvider.isHidden(getTargetPath(path));
    }

    public FileStore getFileStore(Path path)
        throws IOException
    {
        return storageFileSystemProvider.getFileStore(getTargetPath(path));
    }

    public void checkAccess(Path path,
                            AccessMode... modes)
        throws IOException
    {
        storageFileSystemProvider.checkAccess(getTargetPath(path), modes);
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                Class<V> type,
                                                                LinkOption... options)
    {
        return storageFileSystemProvider.getFileAttributeView(getTargetPath(path), type, options);
    }

    public <A extends BasicFileAttributes> A readAttributes(Path path,
                                                            Class<A> type,
                                                            LinkOption... options)
        throws IOException
    {
        return storageFileSystemProvider.readAttributes(getTargetPath(path), type, options);
    }

    public Map<String, Object> readAttributes(Path path,
                                              String attributes,
                                              LinkOption... options)
        throws IOException
    {
        return storageFileSystemProvider.readAttributes(getTargetPath(path), attributes, options);
    }

    public void setAttribute(Path path,
                             String attribute,
                             Object value,
                             LinkOption... options)
        throws IOException
    {
        storageFileSystemProvider.setAttribute(getTargetPath(path), attribute, value, options);
    }

    private Path getTargetPath(Path path)
    {
        return path instanceof RepositoryPath ? ((RepositoryPath) path).getTarget() : path;
    }
}
