package org.carlspring.strongbox.storage.resolvers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author mtodorov
 */
public interface LocationResolver
{

    InputStream getInputStream(String repositoryId,
                               String path)
            throws IOException;

    OutputStream getOutputStream(String repositoryId,
                                 String path)
            throws IOException;

    void delete(String repositoryId,
                String path,
                boolean force)
            throws IOException;

    void deleteTrash(String repositoryId)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void initialize()
            throws IOException;

    String getAlias();

    void setAlias(String alias);

}
