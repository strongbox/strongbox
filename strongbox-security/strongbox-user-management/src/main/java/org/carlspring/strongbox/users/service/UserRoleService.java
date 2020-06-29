package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.domain.UserRole;

/**
 * @author ankit.tomar
 */
public interface UserRoleService
{

    UserRole findOneOrCreate(String role);

}
