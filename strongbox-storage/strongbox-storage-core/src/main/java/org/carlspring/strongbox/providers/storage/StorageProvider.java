package org.carlspring.strongbox.providers.storage;

import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;

/**
 * @author carlspring
 */
public interface StorageProvider
{

    String getAlias();

    void register();

    FileSystem getFileSystem();
    
    FileSystemProvider getFileSystemProvider();

    boolean isCloudBased();

    /**
     * Some cloud providers don't support moving natively. These require a copy and delete operation.
     *
     * @return
     */
    boolean supportsMove();

}
