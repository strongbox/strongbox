package org.carlspring.strongbox.security.jaas;

import org.carlspring.strongbox.security.jaas.authentication.NotSupportedException;

/**
 * @author mtodorov
 */
public interface Group
{

    String getName();

    String getDescription();

    Group getParent() throws NotSupportedException;

}
