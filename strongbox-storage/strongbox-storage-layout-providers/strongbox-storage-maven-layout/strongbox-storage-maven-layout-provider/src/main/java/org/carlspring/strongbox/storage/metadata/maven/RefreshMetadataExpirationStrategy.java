package org.carlspring.strongbox.storage.metadata.maven;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class RefreshMetadataExpirationStrategy
        implements MetadataExpirationStrategy
{

    @Override
    public Decision decide(RepositoryPath repositoryPath)
            throws IOException
    {
        return Decision.EXPIRED;
    }
}
