package org.carlspring.strongbox.authorization.dto;

import org.carlspring.strongbox.users.dto.AccessModelData;

public interface RoleData
{

    String getName();

    String getDescription();

    AccessModelData getAccessModel();

}