package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.storage.repository.Repository;

@FunctionalInterface
public interface RepositoryFileSystemFactory
{

    RepositoryFileSystem create(Repository repository);
    
}
