package org.carlspring.strongbox.providers.layout;

import java.nio.file.spi.FileSystemProvider;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class RawFileSystemProvider extends RepositoryLayoutFileSystemProvider
{

    private static final Logger logger = LoggerFactory.getLogger(RawFileSystemProvider.class);

    @Inject
    private RawLayoutProvider layoutProvider;

    public RawFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);
    }

    @Override
    protected AbstractLayoutProvider getLayoutProvider()
    {
        return layoutProvider;
    }

}
