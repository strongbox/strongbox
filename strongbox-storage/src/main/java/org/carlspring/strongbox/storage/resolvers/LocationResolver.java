package org.carlspring.strongbox.storage.resolvers;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author mtodorov
 */
public interface LocationResolver
{

    InputStream getInputStream(String repository, String path)
            throws IOException;

    void initialize()
            throws IOException;

    String getAlias();

    void setAlias(String alias);

}
