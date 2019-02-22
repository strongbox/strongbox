package org.carlspring.strongbox.authorization.dto;

import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;

public interface RoleReadContract
{

    String getName();

    String getDescription();

    UserAccessModelReadContract getAccessModel();

}