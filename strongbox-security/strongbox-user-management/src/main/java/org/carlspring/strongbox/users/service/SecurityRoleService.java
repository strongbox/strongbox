package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.domain.SecurityRole;

/**
 * @author ankit.tomar
 */
public interface SecurityRoleService
{

    SecurityRole findOneOrCreate(String role);

}
