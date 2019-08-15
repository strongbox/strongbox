package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.userdetails.StrongboxExternalUsersCacheManager;

/**
 * @author sbespalov
 *
 */
public interface UserEntryService extends CrudService<UserEntry, String>, UserService, StrongboxExternalUsersCacheManager
{

}
