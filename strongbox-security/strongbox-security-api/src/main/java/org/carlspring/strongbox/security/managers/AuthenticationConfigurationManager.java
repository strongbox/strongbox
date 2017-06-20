package org.carlspring.strongbox.security.managers;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author mtodorov
 */
public interface AuthenticationConfigurationManager
{

    void load()
            throws IOException, JAXBException;

    void store()
            throws IOException, JAXBException;

}
