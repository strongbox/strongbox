package org.carlspring.strongbox.users.dto;

import java.io.Serializable;
import java.util.Set;

public interface UserAccessModelReadContract extends Serializable
{

    Set<? extends UserStorageReadContract> getStorages();

}
