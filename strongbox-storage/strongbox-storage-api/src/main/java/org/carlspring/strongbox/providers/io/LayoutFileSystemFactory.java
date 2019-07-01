package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.storage.repository.RepositoryData;

@FunctionalInterface
public interface LayoutFileSystemFactory
{

    LayoutFileSystem create(RepositoryData repository);
    
}
