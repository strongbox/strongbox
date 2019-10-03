package org.carlspring.strongbox.storage.metadata.maven;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;

public interface MetadataExpirationStrategy
{

    enum Decision
    {
        UNDECIDED, EXPIRED, USABLE
    }

    Decision decide(final RepositoryPath repositoryPath) throws IOException;
}
