package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.repository.UserRepository;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.apache.commons.lang.StringUtils;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class UserServiceImpl
        implements UserService
{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    UserRepository repository;

    @Inject
    CacheManager cacheManager;

    @Inject
    SecurityTokenProvider tokenProvider;

    @Inject
    OObjectDatabaseTx databaseTx;

    Cache usersCache;

    @PostConstruct
    public void init()
    {

        usersCache = cacheManager.getCache(USERS_CACHE);
        if (usersCache == null)
        {
            throw new BeanCreationException("Unable create users cache");
        }
    }

    @Override
    @Cacheable(value = USERS_CACHE,
               key = "#name",
               sync = true)
    public synchronized User findByUserName(String name)
    {
        try
        {
            return databaseTx.detachAll(repository.findByUsername(name), true);
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception: " + e.getLocalizedMessage());
            logger.trace("Exception details: ", e);

            usersCache.evict(name);

            return null;
        }
    }

    @Override
    @Transactional
    public synchronized <S extends User> S save(S newUser)
    {
        // TODO set unique field constraints on user name
        User existingUser = findByUserName(newUser.getUsername());
        if (existingUser != null)
        {
            delete(existingUser.getObjectId());
        }

        S user = repository.save(newUser);
        usersCache.put(user.getUsername(), user);

        return user;
    }

    @Override
    public synchronized <S extends User> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    public synchronized Optional<User> findOne(String var1)
    {
        if (var1 == null)
        {
            return Optional.empty();
        }

        User user = repository.findOne(var1);
        if (user != null)
        {
            usersCache.put(user.getUsername(), user);
            return Optional.of(user);
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public synchronized boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    public synchronized Optional<List<User>> findAll()
    {
        try
        {
            return Optional.ofNullable(repository.findAll());
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception.", e);
            return Optional.empty();
        }
    }

    @Override
    public synchronized Optional<List<User>> findAll(List<String> var1)
    {
        try
        {
            return Optional.ofNullable(repository.findAll(var1));
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception.", e);
            return Optional.empty();
        }
    }

    @Override
    public synchronized long count()
    {
        return repository.count();
    }

    @Override
    public synchronized void delete(String objectId)
    {
        findOne(objectId).ifPresent(user ->
                                    {
                                        usersCache.evict(user.getUsername());
                                    });

        repository.delete(objectId);
    }

    @Override
    public synchronized void delete(User user)
    {
        usersCache.evict(user.getUsername());
        repository.delete(user);
    }

    @Override
    public synchronized void delete(Iterable<? extends User> var1)
    {
        repository.delete(var1);
    }

    @Override
    public synchronized void deleteAll()
    {
        usersCache.clear();
        repository.deleteAll();
    }

    @Override
    public String generateSecurityToken(String userName)
            throws JoseException
    {

        User user = findByUserName(userName);

        if (StringUtils.isEmpty(user.getSecurityTokenKey()))
        {
            return null;
        }

        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("security-token-key", user.getSecurityTokenKey());

        return tokenProvider.getToken(userName, claimMap, null);
    }

    @Override
    public String generateAuthenticationToken(String userName,
                                              Integer expireMinutes)
            throws JoseException
    {
        User user = findByUserName(userName);

        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("credentials", user.getPassword());

        return tokenProvider.getToken(userName, claimMap, expireMinutes);
    }

    @Override
    public void verifySecurityToken(String userName,
                                    String apiKey)
    {
        User user = findByUserName(userName);

        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("security-token-key", user.getSecurityTokenKey());

        tokenProvider.verifyToken(apiKey, userName, claimMap);
    }

}
