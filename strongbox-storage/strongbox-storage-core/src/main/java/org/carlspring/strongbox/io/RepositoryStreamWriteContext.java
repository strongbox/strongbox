package org.carlspring.strongbox.io;

import java.io.InputStream;
import java.io.OutputStream;

public class RepositoryStreamWriteContext extends RepositoryStreamContext
{

    private OutputStream stream;

    public OutputStream getStream()
    {
        return stream;
    }

    public void setStream(OutputStream stream)
    {
        this.stream = stream;
    }

}
