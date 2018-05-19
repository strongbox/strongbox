package org.carlspring.strongbox.io;

import java.io.IOException;

public interface RepositoryStreamCallback
{

    void onBeforeRead(RepositoryStreamContext ctx) throws IOException;

    void onBeforeWrite(RepositoryStreamContext ctx) throws IOException;
    
    void onAfterClose(RepositoryStreamContext ctx) throws IOException;

}
