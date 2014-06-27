package org.carlspring.strongbox.security.jaas.authorization;

import org.carlspring.strongbox.security.jaas.Group;

/**
 * @author mtodorov
 */
public interface GroupResolver
{

    Group getGroup(String name);

}
