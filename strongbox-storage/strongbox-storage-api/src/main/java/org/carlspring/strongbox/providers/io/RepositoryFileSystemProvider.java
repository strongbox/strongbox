package org.carlspring.strongbox.providers.io;

import java.io.File;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a proxy implementation which wraps target {@link FileSystemProvider}. <br>
 * Note that almost all {@link Path} operations in this implementation must delegate method invocations into
 * {@link Files} utility class with {@link RepositoryPath}'s target as parameters.
 *
 * @author Sergey Bespalov
 */
public abstract class RepositoryFileSystemProvider
        extends FileSystemProvider
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFileSystemProvider.class);

    private FileSystemProvider storageFileSystemProvider;
    private boolean allowsForceDelete;
    
    public RepositoryFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super();
        this.storageFileSystemProvider = storageFileSystemProvider;
    }

    public boolean isAllowsForceDelete()
    {
        return allowsForceDelete;
    }

    public void setAllowsForceDelete(boolean forceDelete)
    {
        this.allowsForceDelete = forceDelete;
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
        return storageFileSystemProvider.newByteChannel(unwrap(path), options, attrs);
    }

    public DirectoryStream<Path> newDirectoryStream(Path dir,
                                                    Filter<? super Path> filter)
            throws IOException
    {
        DirectoryStream<Path> ds = storageFileSystemProvider.newDirectoryStream(unwrap(dir), filter);
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
        storageFileSystemProvider.createDirectory(unwrap(dir), attrs);
    }

    public void delete(Path path) throws IOException{
        delete(path, isAllowsForceDelete());
    }

    public void delete(Path path, boolean force)
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
            throw new NoSuchFileException(unwrap(repositoryPath).toString());
        }

        if (!Files.isDirectory(repositoryPath))
        {
            doDeletePath(repositoryPath, force, true);
        }
        else
        {
            Files.walkFileTree(repositoryPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                        throws IOException
                {
                    //Checksum files will be deleted during directory walking
                    doDeletePath((RepositoryPath) file, force, false);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc)
                        throws IOException
                {
                    Files.delete(unwrap(dir));
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    protected void doDeletePath(RepositoryPath repositoryPath,
                                boolean force,
                                boolean deleteChacksum)
            throws IOException
    {
        doDeletePath(repositoryPath, force);
        if (!deleteChacksum){
            return;
        }
        for (String digestAlgorithm : repositoryPath.getFileSystem().getDigestAlgorithmSet())
        {
            //it creates Checksum file extension name form Digest algorithm name: SHA-1->sha1
            String extension = digestAlgorithm.replaceAll("-", "").toLowerCase();
            RepositoryPath checksumPath = repositoryPath.resolveSibling(repositoryPath.getFileName() + "." + extension);
            if (Files.exists(unwrap(checksumPath)))
            {
                doDeletePath(checksumPath, force);
            }
        }
    }
    
    protected void doDeletePath(RepositoryPath repositoryPath,
                                boolean force)
            throws IOException
    {
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
        
        if (force && repository.allowsForceDeletion())
        {
            deleteTrash(repositoryPath);
        }
    }    
    
    public void undelete(RepositoryPath path)
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
                               new MoveDirectoryVisitor(trashPath.getTarget(),
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
            if (!Files.exists(unwrap(path).getParent()))
            {
                Files.createDirectories(unwrap(path).getParent());
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
        else
        {
            File trashFile = trashPath.getTarget().toFile();

            FileUtils.deleteDirectory(trashFile);

            //noinspection ResultOfMethodCallIgnored
            trashFile.mkdirs();
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
        String sourceRelative = source.getRoot().relativize(source.getTarget()).toString();

        // XXX[SBESPALOV]: We try to convert path from source to target FileSystem and need to check this on different
        // Storage types.
        // Note that this is only draft implementation, and probably in the future we will need something like separate
        // `FileSystemPathConverter` to convert Paths from one FileSystem to another. Such a `FileSystemPathConverter`
        // can be provided by the `RepositoryFileSystem` instance.

        String sTargetPath = sourceRelative;

        return targetBase.resolve(sTargetPath);
    }

    @Override
    public InputStream newInputStream(Path path,
                                      OpenOption... options)
            throws IOException
    {
        return super.newInputStream(unwrap(path), options);
    }

    @Override
    public OutputStream newOutputStream(Path path,
                                        OpenOption... options)
            throws IOException
    {
        return super.newOutputStream(unwrap(path), options);
    }

    public void copy(Path source,
                     Path target,
                     CopyOption... options)
            throws IOException
    {
        storageFileSystemProvider.copy(unwrap(source), unwrap(target), options);
    }

    public void move(Path source,
                     Path target,
                     CopyOption... options)
            throws IOException
    {
        storageFileSystemProvider.move(unwrap(source), unwrap(target), options);
    }

    public boolean isSameFile(Path path,
                              Path path2)
            throws IOException
    {
        return storageFileSystemProvider.isSameFile(unwrap(path), unwrap(path2));
    }

    public boolean isHidden(Path path)
            throws IOException
    {
        return storageFileSystemProvider.isHidden(unwrap(path));
    }

    public FileStore getFileStore(Path path)
            throws IOException
    {
        return storageFileSystemProvider.getFileStore(unwrap(path));
    }

    public void checkAccess(Path path,
                            AccessMode... modes)
            throws IOException
    {
        storageFileSystemProvider.checkAccess(unwrap(path), modes);
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                Class<V> type,
                                                                LinkOption... options)
    {
        return storageFileSystemProvider.getFileAttributeView(unwrap(path), type, options);
    }

    public <A extends BasicFileAttributes> A readAttributes(Path path,
                                                            Class<A> type,
                                                            LinkOption... options)
        throws IOException
    {
        A targetAttributes = storageFileSystemProvider.readAttributes(unwrap(path), type, options);
        if (!(path instanceof RepositoryPath))
        {
            return targetAttributes;
        }
        RepositoryPath repositoryPath = (RepositoryPath) path;
        RepositoryPath repositoryRelativePath = repositoryPath.getRepositoryRelative();
        
        RepositoryFileAttributes repositoryFileAttributes = new RepositoryFileAttributes(targetAttributes,
                getRepositoryFileAttributes(repositoryRelativePath));
        return (A) repositoryFileAttributes;
    }

    protected abstract Map<String,Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath);
    
    public boolean isChecksum(RepositoryPath path)
    {
        for (String e : path.getFileSystem().getDigestAlgorithmSet())
        {
            if (path.getFileName().toString().endsWith("." + e.replaceAll("-", "").toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }
    
    public Map<String, Object> readAttributes(Path path,
                                              String attributes,
                                              LinkOption... options)
            throws IOException
    {
        if (!(path instanceof RepositoryPath))
        {
            return storageFileSystemProvider.readAttributes(unwrap(path), attributes, options);
        }
        //TODO: Make an implementation in accordance with the specification
        RepositoryPath repositoryPath = (RepositoryPath) path;
        RepositoryPath repositoryRelativePath = repositoryPath.getRepositoryRelative();
        return getRepositoryFileAttributes(repositoryRelativePath);
    }

    public void setAttribute(Path path,
                             String attribute,
                             Object value,
                             LinkOption... options)
            throws IOException
    {
        storageFileSystemProvider.setAttribute(unwrap(path), attribute, value, options);
    }

    private Path unwrap(Path path)
    {
        return path instanceof RepositoryPath ? ((RepositoryPath) path).getTarget() : path;
    }

    public static class MoveDirectoryVisitor
            extends SimpleFileVisitor<Path>
    {

        private final Path fromPath;
        private final Path toPath;
        private final CopyOption copyOption;

        public MoveDirectoryVisitor(Path fromPath,
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