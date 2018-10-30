package org.carlspring.strongbox.io;

import org.carlspring.strongbox.providers.datastore.StorageProvider;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;

/**
 * This class decoretes storage {@link FileSystem} implementation.
 *
 * @author Sergey Bespalov
 * 
 * @see StorageProvider
 */
public abstract class StorageFileSystem
        extends FileSystem
{

    private FileSystem target;

    public StorageFileSystem(FileSystem target)
    {
        this.target = target;
    }

    protected FileSystem getTarget()
    {
        return target;
    }

    public FileSystemProvider provider()
    {
        return target.provider();
    }

    public void close()
            throws IOException
    {
        target.close();
    }

    public boolean isOpen()
    {
        return target.isOpen();
    }

    public boolean isReadOnly()
    {
        return target.isReadOnly();
    }

    public String getSeparator()
    {
        return target.getSeparator();
    }

    public Iterable<Path> getRootDirectories()
    {
        ArrayList<Path> result = new ArrayList<Path>();
        result.add(getRootDirectory());
        return result;
    }

    public abstract Path getRootDirectory();

    public Iterable<FileStore> getFileStores()
    {
        return target.getFileStores();
    }

    public Set<String> supportedFileAttributeViews()
    {
        return target.supportedFileAttributeViews();
    }

    public Path getPath(String first,
                        String... more)
    {
        return target.getPath(first, more);
    }

    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        return target.getPathMatcher(syntaxAndPattern);
    }

    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        return target.getUserPrincipalLookupService();
    }

    public WatchService newWatchService()
            throws IOException
    {
        return target.newWatchService();
    }

}
