package org.carlspring.strongbox.security.jaas.managers;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface ConfigurationManager
{

    void load() throws IOException;

    void store() throws IOException;

}
