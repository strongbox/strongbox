package org.carlspring.strongbox.providers.io;

import java.io.IOException;

public enum RefreshMetadataStrategy implements MetadataStrategy
{
    INSTANCE;

    @Override
    public Decision determineMetadataRefetch(RepositoryPath repositoryPath)
            throws IOException
    {
        return Decision.YES_FETCH;
    }
}
