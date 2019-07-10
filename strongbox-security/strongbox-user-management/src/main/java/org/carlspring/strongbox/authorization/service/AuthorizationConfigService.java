package org.carlspring.strongbox.authorization.service;

import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.domain.AuthorizationConfig;

import java.io.IOException;
import java.util.List;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
public interface AuthorizationConfigService
{

    void setAuthorizationConfig(AuthorizationConfigDto config) throws IOException;

    AuthorizationConfigDto getDto();

    AuthorizationConfig get();

    void addRole(RoleDto role) throws IOException;

    boolean deleteRole(String roleName) throws IOException;

    void addPrivilegesToAnonymous(List<Privileges> privilegeList) throws IOException;
}
