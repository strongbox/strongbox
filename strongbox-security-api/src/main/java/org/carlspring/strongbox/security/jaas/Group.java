package org.carlspring.strongbox.security.jaas;

/**
 * @author mtodorov
 */
public interface Group
{

    String getName();

    String getDescription();

    Group getParent();

}
