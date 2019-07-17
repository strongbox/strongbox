package org.carlspring.strongbox.users.userdetails;

import org.carlspring.strongbox.users.dto.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This interface purpose is to manage strongbox external users cache within authentication components.  
 * 
 * @author sbespalov
 */
public interface StrongboxUserManager
{

    User findByUsername(String username);
    
    User updateExternalUserDetails(String externalSourceId, UserDetails user);

    void deleteByUsername(String username);
    
}
