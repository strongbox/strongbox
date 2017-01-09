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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
        DirectoryStream<Path> ds = storageFileSystemProvider.newDirectoryStream(getTargetPath(dir), filter);
        if (!(dir instanceof RepositoryPath))
        {
            return ds;
        }
        RepositoryPath repositoryDir = (RepositoryPath) dir;
        return new DirectoryStream<Path>()
        {

            @Override
            public void close()
                throws IOException
            {
                ds.close();
            }

            @Override
            public Iterator<Path> iterator()
            {
                return createRepositoryDsIterator(repositoryDir.getFileSystem(), ds.iterator());
            }

        };
    }

    protected Iterator<Path> createRepositoryDsIterator(RepositoryFileSystem rfs,
                                                        Iterator<Path> iterator)
    {

        return new Iterator<Path>()
        {

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Path next()
            {
                return new RepositoryPath(iterator.next(), rfs);
            }

            @Override
            public void remove()
            {
                iterator.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super Path> action)
            {
                iterator.forEachRemaining(action);
            }

        };
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
            throw new NoSuchFileException(getTargetPath(repositoryPath).toString());
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
        if (!Files.exists(trashPath.getTarget()))
        {
            return;
        }

        if (!Files.isDirectory(trashPath.getTarget()))
        {
            Files.move(trashPath.getTarget(), path.getTarget(), StandardCopyOption.REPLACE_EXISTING);
        }
        else
        {
            Files.walkFileTree(trashPath.getTarget(),
                               new MoveDirVisitor(trashPath.getTarget(),
                                       path.getTarget(),
                                       StandardCopyOption.REPLACE_EXISTING));
        }
    }

    public void restoreFromTemp(RepositoryPath path)
        throws IOException
    {
        RepositoryPath tempPath = getTempPath(path);
        if (Files.exists(tempPath.getTarget()))
        {
            if (!Files.exists(getTargetPath(path).getParent()))
            {
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
        if (!Files.exists(trashPath.getTarget()))
        {
            return;
        }
        if (!Files.isDirectory(trashPath.getTarget()))
        {
            Files.delete(trashPath.getTarget());
        }
        else
        {
            Files.walkFileTree(trashPath.getTarget(), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                    throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc)
                    throws IOException
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public RepositoryPath getTempPath(RepositoryPath path)
        throws IOException
    {
        RepositoryPath tempPathBase = path.getFileSystem().getTempPath();
        RepositoryPath tempPath = rebase(path, tempPathBase);
        if (!Files.exists(tempPath.getParent().getTarget()))
        {
            logger.debug(String.format("Creating: dir-[%s]", tempPath.getParent()));

            Files.createDirectories(tempPath.getParent().getTarget());
        }

        return tempPath;
    }

    public RepositoryPath getTrashPath(RepositoryPath path)
        throws IOException
    {
        RepositoryPath trashBasePath = path.getFileSystem().getTrashPath();
        RepositoryPath trashPath = rebase(path, trashBasePath);
        if (!Files.exists(trashPath.getParent().getTarget()))
        {
            logger.debug(String.format("Creating: dir-[%s]", trashPath.getParent()));
            Files.createDirectories(trashPath.getParent().getTarget());
        }

        return trashPath;
    }

    private static RepositoryPath rebase(RepositoryPath source,
                                         RepositoryPath targetBase)
    {
        FileSystem sourceFileSystem = source.getTarget().getFileSystem();
        FileSystem targetFileSystem = targetBase.getTarget().getFileSystem();

        String sourceRelative = source.getRoot()
                                      .relativize(source.getTarget())
                                      .toString();

        // XXX[SBESPALOV]: We try to convert path form source to target FileSystem and need to check this on different
        // Storage types.
        // Note that this is only draft implementation, and probably in the future we will need something like separate
        // `FileSystemPathConverter` to convert Paths from one FileSystem to another. Such `FileSystemPathConverter` can
        // be provided by the `ReposytoryFileSystem` instance.
        String sTargetPath = sourceRelative.replaceAll(sourceFileSystem.getSeparator(),
                                                       targetFileSystem.getSeparator());

        return targetBase.resolve(sTargetPath);
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

    public static class MoveDirVisitor extends SimpleFileVisitor<Path>
    {
        private final Path fromPath;
        private final Path toPath;
        private final CopyOption copyOption;

        public MoveDirVisitor(Path fromPath,
                              Path toPath,
                              CopyOption copyOption)
        {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs)
            throws IOException
        {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath))
            {
                Files.createDirectory(targetPath);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
            throws IOException
        {
            Files.move(file, toPath.resolve(fromPath.relativize(file)), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

}
