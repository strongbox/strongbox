package org.carlspring.strongbox.providers.layout;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.spi.FileSystemProvider;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class RawFileSystemProvider extends LayoutFileSystemProvider
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

	@Override
	protected void deleteMetadata(RepositoryPath repositoryPath) throws IOException {
		
	}

}
