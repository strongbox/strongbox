package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.lang.StringUtils;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
public class UserServiceImpl
        extends CommonCrudService<User>
        implements UserService
{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private SecurityTokenProvider tokenProvider;

    @Override
    @Cacheable(value = CacheName.User.USERS, key = "#name")
    public User findByUserName(String name)
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", name);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<Long> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<User> resultList = getDelegate().command(oQuery).execute(params);
        return !resultList.isEmpty() ? resultList.iterator().next() : null;
    }

    @Override
    @CacheEvict(value = { CacheName.User.USERS,
                          CacheName.User.USER_DETAILS }, key = "#newUser.username")
    public <S extends User> S save(S newUser)
    {
        return super.save(newUser);
    }

    @Override
    public Optional<User> findOne(String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        Optional<User> optionalUser = super.findOne(id);

        optionalUser.ifPresent(user -> cacheManager.getCache(CacheName.User.USERS).put(user.getUsername(), user));

        return optionalUser;
    }

    @Override
    public void delete(String objectId)
    {
        Optional<User> optionalUser = findOne(objectId);
        optionalUser.ifPresent(user -> self().delete(optionalUser.get()));
    }

    @CacheEvict(value = { CacheName.User.USERS,
                          CacheName.User.USER_DETAILS }, key = "#user.username")
    @Override
    public void delete(User user)
    {
        super.delete(user);
    }

    @CacheEvict(value = { CacheName.User.USERS,
                          CacheName.User.USER_DETAILS }, allEntries = true)
    @Override
    public void deleteAll()
    {
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

    @Override
    public User updatePassword(User userToUpdate)
    {
        final UserService self = self();
        User user = self.findByUserName(userToUpdate.getUsername());
        updatePassword(user, userToUpdate.getPassword());
        return self.save(user);
    }

    @Override
    public User updateByUsername(User userToUpdate)
    {
        final UserService self = self();
        User user = self.findByUserName(userToUpdate.getUsername());
        updatePassword(user, userToUpdate.getPassword());
        user.setEnabled(userToUpdate.isEnabled());
        user.setRoles(userToUpdate.getRoles());
        user.setSecurityTokenKey(userToUpdate.getSecurityTokenKey());
        user.setAccessModel(userToUpdate.getAccessModel());
        return self.save(user);
    }

    private UserService self()
    {
        return applicationContext.getBean(UserService.class);
    }

    private void updatePassword(User user,
                                String rawPassword)
    {
        if (StringUtils.isNotEmpty(rawPassword))
        {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    @Override
    public Class<User> getEntityClass()
    {
        return User.class;
    }

}
