package org.carlspring.strongbox.authorization.service;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.domain.AuthorizationConfig;

import java.util.List;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
public interface AuthorizationConfigService
{

    String ANONYMOUS_ROLE = "ANONYMOUS_ROLE";

    void setAuthorizationConfig(AuthorizationConfigDto config);

    AuthorizationConfigDto getDto();

    AuthorizationConfig get();

    void addRole(RoleDto role);

    boolean deleteRole(String roleName);

    void addPrivilegesToAnonymous(List<PrivilegeDto> privilegeList);
}
