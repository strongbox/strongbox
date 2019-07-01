package org.carlspring.strongbox.users.dto;

import java.util.Date;
import java.util.Set;

public interface UserData
{

    String getUsername();

    String getPassword();

    Set<String> getRoles();

    String getSecurityTokenKey();

    boolean isEnabled();

    Date getLastUpdate();

}