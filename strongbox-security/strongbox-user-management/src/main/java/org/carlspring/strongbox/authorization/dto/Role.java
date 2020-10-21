package org.carlspring.strongbox.authorization.dto;

import org.carlspring.strongbox.users.dto.AccessModel;

public interface Role
{

    String getName();

    String getDescription();

    AccessModel getAccessModel();

}