package org.carlspring.strongbox.providers.io;

import java.io.IOException;

public interface RepositoryPathHandler
{

    void postProcess(RepositoryPath path) throws IOException;
    
}
