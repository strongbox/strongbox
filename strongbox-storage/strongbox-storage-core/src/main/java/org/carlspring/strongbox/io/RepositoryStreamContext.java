package org.carlspring.strongbox.io;

import org.carlspring.strongbox.storage.repository.Repository;

public interface RepositoryStreamContext
{

    Repository getRepository();
    
    String getPath();
    
}
