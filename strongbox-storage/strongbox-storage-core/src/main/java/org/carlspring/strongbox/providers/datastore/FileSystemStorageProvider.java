package org.carlspring.strongbox.providers.datastore;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("filesystemStorageProvider")
public class FileSystemStorageProvider extends AbstractStorageProvider
{

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageProvider.class);

    public static final String ALIAS = StorageProviderEnum.FILESYSTEM.describe();

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @PostConstruct
    @Override
    public void register()
    {
        logger.info("Registered storage provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public FileSystem getFileSystem()
    {
        return FileSystems.getDefault();
    }

    @Override
    public FileSystemProvider getFileSystemProvider()
    {
        List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();
        for (FileSystemProvider fileSystemProvider : installedProviders)
        {
            if (!"file".equals(fileSystemProvider.getScheme()))
            {
                continue;
            }

            return fileSystemProvider;
        }

        return null;
    }

}
