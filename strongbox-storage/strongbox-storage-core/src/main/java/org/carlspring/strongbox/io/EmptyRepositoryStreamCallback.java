package org.carlspring.strongbox.io;

import java.io.IOException;

public class EmptyRepositoryStreamCallback implements RepositoryStreamCallback
{

    @Override
    public void onBeforeRead(RepositoryStreamContext ctx)
        throws IOException
    {

    }

    @Override
    public void onBeforeWrite(RepositoryStreamContext ctx)
        throws IOException
    {

    }

    @Override
    public void onAfterClose(RepositoryStreamContext ctx)
        throws IOException
    {

    }

}
