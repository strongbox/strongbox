package org.carlspring.strongbox.providers.io;

import java.io.IOException;

public interface MetadataStrategy
{

    enum Decision
    {
        I_DONT_KNOW, YES_FETCH, NO_LEAVE_IT
    }

    Decision determineMetadataRefetch(final RepositoryPath repositoryPath) throws IOException;
}
