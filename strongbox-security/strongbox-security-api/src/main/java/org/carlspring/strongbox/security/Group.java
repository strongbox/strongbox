package org.carlspring.strongbox.security;

import org.carlspring.strongbox.security.exceptions.NotSupportedException;

/**
 * @author mtodorov
 */
public interface Group
{

    String getName();

    String getDescription();

    Group getParent() throws NotSupportedException;

}
