package org.carlspring.strongbox.io;

public interface RepositoryStreamCallback
{

    void onBeforeRead(RepositoryStreamContext ctx);

    void onBeforeWrite(RepositoryStreamContext ctx);
    
    void onAfterClose(RepositoryStreamContext ctx);

}
