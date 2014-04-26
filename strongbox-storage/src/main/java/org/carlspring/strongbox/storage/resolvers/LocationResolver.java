package org.carlspring.strongbox.storage.resolvers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author mtodorov
 */
public interface LocationResolver
{

    InputStream getInputStream(String repository, String path)
            throws IOException;

    OutputStream getOutputStream(String repository, String path)
            throws IOException;

    void delete(String repository, String path)
            throws IOException;

    void deleteTrash(String repository)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void initialize()
            throws IOException;

    String getAlias();

    void setAlias(String alias);

}
