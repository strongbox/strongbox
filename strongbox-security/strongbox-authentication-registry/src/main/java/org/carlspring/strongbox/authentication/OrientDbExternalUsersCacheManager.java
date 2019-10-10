package org.carlspring.strongbox.authentication;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.service.UserAlreadyExistsException;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService;
import org.carlspring.strongbox.users.userdetails.StrongboxExternalUsersCacheManager;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

/**
 * @author sbespalov
 */
@Component
public class OrientDbExternalUsersCacheManager extends OrientDbUserService implements StrongboxExternalUsersCacheManager
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbExternalUsersCacheManager.class);
    
    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p1.username")
    public User cacheExternalUserDetails(String sourceId,
                                         UserDetails springUser)
    {
        User user = springUser instanceof StrongboxUserDetails ? ((StrongboxUserDetails) springUser).getUser()
                : new UserData(springUser);
        String username = user.getUsername();
        
        Optional<UserEntry> optionalUser = Optional.ofNullable(findByUsername(user.getUsername())).map(this::detach);
        try
        {
            // If found user was from another source then remove before save
            if (optionalUser.map(User::getSourceId).map(sourceId::equals).filter(Boolean.FALSE::equals).isPresent())
            {
                delete(optionalUser.get());
                optionalUser = Optional.empty();
            }
            
            UserEntry userEntry = optionalUser.orElseGet(() -> new UserEntry());
            
            if (!StringUtils.isBlank(user.getPassword()))
            {
                userEntry.setPassword(user.getPassword());
            }
            userEntry.setUsername(username);
            userEntry.setEnabled(user.isEnabled());
            userEntry.setRoles(user.getRoles());
            userEntry.setSecurityTokenKey(user.getSecurityTokenKey());
            userEntry.setLastUpdate(new Date());
            userEntry.setSourceId(sourceId);

            return cascadeEntitySave(userEntry);
        }
        catch (ONeedRetryException|ORecordNotFoundException e)
        {
            logger.debug(String.format("Retry to save user [%s] from [%s] by reason [%s]",
                                       user.getUsername(), sourceId, e.getMessage()));

            return cacheExternalUserDetails(sourceId, springUser);
        }
        catch (ORecordDuplicatedException e)
        {
            
            throw new UserAlreadyExistsException(String.format("Failed to cache external user from [%s], duplicate [%s] already exists.", sourceId,
                                                               user.getUsername()),
                                                 e);
        }
    }

    
}
