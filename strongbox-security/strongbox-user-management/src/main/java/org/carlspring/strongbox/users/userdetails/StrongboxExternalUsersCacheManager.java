package org.carlspring.strongbox.users.userdetails;

import org.carlspring.strongbox.users.dto.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This interface purpose is to manage strongbox external users cache within
 * authentication components.<br>
 * Extarnal users stored near with regular users but the difference is that
 * external users have not empty value of {@link User#getSourceId()}, which
 * pointing to external system ID where the external user come from.<br>
 * Another thing specific for external users is that they have configurable
 * cache invalidation time, so when it expires then the external user should be
 * invalidated and deleted. So the value of {@link User#getLastUpdate()} updated
 * every time
 * {@link StrongboxExternalUsersCacheManager#updateExternalUserDetails(String, UserDetails)}
 * method called, this extends the period during which the external user remains
 * valid.
 * 
 * @author sbespalov
 */
public interface StrongboxExternalUsersCacheManager
{

    User findByUsername(String username);

    User updateExternalUserDetails(String externalSourceId,
                                   UserDetails user);

    void deleteByUsername(String username);

}
