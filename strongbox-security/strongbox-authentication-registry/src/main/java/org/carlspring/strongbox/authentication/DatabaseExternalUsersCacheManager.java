package org.carlspring.strongbox.authentication;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.User;
import org.carlspring.strongbox.domain.UserEntity;
import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.service.UserAlreadyExistsException;
import org.carlspring.strongbox.users.service.impl.DatabaseUserService;
import org.carlspring.strongbox.users.userdetails.StrongboxExternalUsersCacheManager;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetails;
import org.carlspring.strongbox.util.LocalDateTimeInstance;

import javax.transaction.Transactional;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.janusgraph.core.SchemaViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
@Transactional
public class DatabaseExternalUsersCacheManager extends DatabaseUserService implements StrongboxExternalUsersCacheManager
{
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseExternalUsersCacheManager.class);


    @Override
    public UserEntity findByUsername(String username)
    {
        UserEntity result = super.findByUsername(username);
        if (result != null)
        {
            logger.debug("User found in DB cache: username=[{}], sourceId=[{}], id=[{}], uuid=[{}]",
                         result.getUsername(),
                         result.getSourceId(),
                         result.getNativeId(),
                         result.getUuid());
        }
        else
        {
            logger.debug("User not found in DB cache: username=[{}]", username);
        }
        
        return result;
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p1.username")
    public User cacheExternalUserDetails(String sourceId,
                                         UserDetails springUser)
    {
        User user;
        if (springUser instanceof StrongboxUserDetails)
        {
            user = ((StrongboxUserDetails) springUser).getUser();
        }
        else
        {
            user = new UserData(springUser);
        }
        String username = user.getUsername();
        logger.info("Cache external user: username=[{}], id=[{}], uuid=[{}], sourceId=[{}], UserDetails=[{}]",
                    username,
                    user.getNativeId(),
                    user.getUuid(),
                    sourceId,
                    springUser.getClass().getSimpleName());
        
        Optional<UserEntity> oldUser = Optional.ofNullable(findByUsername(user.getUsername()));
        try
        {
            // If found user was from another source then remove before save
            Optional<String> oldSource = oldUser.map(User::getSourceId);
            if (oldSource.map(sourceId::equals).filter(Boolean.FALSE::equals).isPresent())
            {
                logger.debug("Invalidate user from another source: username=[{}], oldSource=[{}], newSource=[{}]",
                             username,
                             oldSource.get(),
                             sourceId);
                deleteByUsername(oldUser.map(u -> user.getUuid()).get());
                oldUser = Optional.empty();
            }
            
            UserEntity userEntry = oldUser.orElseGet(() -> new UserEntity(username));
            
            if (!StringUtils.isBlank(user.getPassword()))
            {
                userEntry.setPassword(user.getPassword());
            }
            userEntry.setEnabled(user.isEnabled());
            userEntry.setRoles(user.getRoles());
            userEntry.setSecurityTokenKey(user.getSecurityTokenKey());
            userEntry.setLastUpdated(LocalDateTimeInstance.now());
            userEntry.setSourceId(sourceId);

            UserEntity result = userRepository.save(userEntry);
            logger.debug("Cached user: username=[{}], id=[{}], uuid=[{}], sourceId=[{}], lastUpdated=[{}]",
                         result.getUsername(),
                         result.getNativeId(),
                         result.getUuid(),
                         result.getSourceId(),
                         result.getLastUpdated());
            
            return result;
        }
        catch (SchemaViolationException e)
        {
            
            throw new UserAlreadyExistsException(String.format("Failed to cache external user from [%s], duplicate [%s] already exists.", sourceId,
                                                               user.getUsername()),
                                                 e);
        }
    }

    
}
