package org.carlspring.strongbox.security.jaas.authorization;

import org.carlspring.strongbox.security.jaas.Group;

/**
 * @author mtodorov
 */
public interface GroupStorage
{

    void createGroup(Group group) throws GroupStorageException;

    void updateGroup(Group group) throws GroupStorageException;

    void removeGroup(Group group) throws GroupStorageException;

    void removeGroup(String groupName) throws GroupStorageException;

}
