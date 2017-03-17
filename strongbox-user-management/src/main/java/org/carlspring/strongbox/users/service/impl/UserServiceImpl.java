package org.carlspring.strongbox.users.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * DAO implementation for {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
public class UserServiceImpl extends CommonCrudService<User>
        implements UserService
{

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final String USERS_CACHE = "users";

    @Inject
    CacheManager cacheManager;
    @Inject
    SecurityTokenProvider tokenProvider;
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
    public Class<User> getEntityClass()
    {
        return User.class;
    }

    @Override
    @Cacheable(value = USERS_CACHE, key = "#name", sync = true)
    public User findByUserName(String name)
    {
        String sQuery = String.format("select * from %s where userName=:userName", getEntityClass().getSimpleName(),
                                      name);
        OSQLSynchQuery<Long> oQuery = new OSQLSynchQuery<Long>(sQuery);
        oQuery.setLimit(1);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("userName", name);

        List<User> resultList = getDelegate().command(oQuery).execute(params);
        return resultList.size() > 0 ? resultList.iterator().next() : null;
    }

    @Override
    public <S extends User> S save(S newUser)
    {
        S user = super.save(newUser);
        usersCache.put(user.getUsername(), getDelegate().detachAll(user, true));
        return user;
    }

    @Override
    public Optional<User> findOne(String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        Optional<User> optionalUser = super.findOne(id);
        return optionalUser.map(u -> {
            usersCache.put(u.getUsername(), getDelegate().detachAll(u, true));
            return Optional.ofNullable(u);
        }).orElse(optionalUser);
    }

    @Override
    public void delete(String objectId)
    {
        findOne(objectId).ifPresent(user -> {
            usersCache.evict(user.getUsername());
        });
        super.delete(objectId);
    }

    @Override
    public void delete(User user)
    {
        usersCache.evict(user.getUsername());
        super.delete(user);
    }

    @Override
    public void deleteAll()
    {
        usersCache.clear();
        super.deleteAll();
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