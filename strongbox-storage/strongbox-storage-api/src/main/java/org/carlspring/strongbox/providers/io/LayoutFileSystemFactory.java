package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.storage.repository.Repository;

@FunctionalInterface
public interface LayoutFileSystemFactory
{

    LayoutFileSystem create(Repository repository);
    
}
