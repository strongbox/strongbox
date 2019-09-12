package org.carlspring.strongbox.providers.io;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class RefreshMetadataStrategy implements MetadataStrategy
{

    @Override
    public Decision determineMetadataRefetch(RepositoryPath repositoryPath)
            throws IOException
    {
        return Decision.YES_FETCH;
    }
}
