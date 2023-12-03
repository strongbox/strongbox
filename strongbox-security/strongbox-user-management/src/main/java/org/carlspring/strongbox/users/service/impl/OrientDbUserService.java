package org.carlspring.strongbox.users.service.impl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserEntryService;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * @author sbespalov
 */
@Component
@OrientDb
public class OrientDbUserService extends CommonCrudService<UserEntry> implements UserEntryService
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbUserService.class);

    @Inject
    private SecurityTokenProvider tokenProvider;

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p0")
    public void deleteByUsername(String username)
    {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);

        String sQuery = String.format("delete from %s where username = :username", UserEntry.class.getSimpleName());

        OCommandSQL oQuery = new OCommandSQL(sQuery);
        getDelegate().command(oQuery).execute(params);
    }

    @Override
    public UserEntry findByUsername(String username)
    {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<UserEntry> resultList = getDelegate().command(oQuery).execute(params);

        return resultList.stream().findFirst().orElse(null);

    }

    @Override
    public String generateSecurityToken(String username)
        throws JoseException
    {
        final User user = findByUsername(username);

        if (StringUtils.isEmpty(user.getSecurityTokenKey()))
        {
            return null;
        }

        final Map<String, String> claimMap = new HashMap<>();
        claimMap.put(UserData.SECURITY_TOKEN_KEY, user.getSecurityTokenKey());

        return tokenProvider.getToken(username, claimMap, null, null);
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p0.username")
    public void updateAccountDetailsByUsername(User userToUpdate)
    {
        UserEntry user = findByUsername(userToUpdate.getUsername());
        if (user == null)
        {
            throw new UsernameNotFoundException(userToUpdate.getUsername());
        }

        if (!StringUtils.isBlank(userToUpdate.getPassword()))
        {
            user.setPassword(userToUpdate.getPassword());
        }

        if (StringUtils.isNotBlank(userToUpdate.getSecurityTokenKey()))
        {
            user.setSecurityTokenKey(userToUpdate.getSecurityTokenKey());
        }

        save(user);
    }

    @Override
    public Users getUsers()
    {
        Optional<List<UserEntry>> allUsers = findAll();
        if (allUsers.isPresent())
        {
            return new Users(allUsers.get().stream().map(u -> detach(u)).collect(Collectors.toSet()));
        }

        return null;
    }

    @Override
    public void revokeEveryone(String roleToRevoke)
    {
        Map<String, String> params = new HashMap<>();
        params.put("role", roleToRevoke);

        String sQuery = String.format("select * from %s where :role in roles", UserEntry.class.getSimpleName());

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        List<UserEntry> resultList = getDelegate().command(oQuery).execute(params);

        resultList.stream().forEach(user -> {
            detach(user).getRoles().remove(roleToRevoke);
            save(user);
        });
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p0.username")
    public User save(User user)
    {
        UserEntry userEntry = Optional.ofNullable(findByUsername(user.getUsername())).orElseGet(() -> new UserEntry());

        if (!StringUtils.isBlank(user.getPassword()))
        {
            userEntry.setPassword(user.getPassword());
        }
        userEntry.setUsername(user.getUsername());
        userEntry.setEnabled(user.isEnabled());
        userEntry.setRoles(user.getRoles());
        userEntry.setSecurityTokenKey(user.getSecurityTokenKey());
        userEntry.setLastUpdate(new Date());

        return save(userEntry);
    }

    @Override
    @CacheEvict(cacheNames = CacheName.User.AUTHENTICATIONS, key = "#p0.username")
    public <S extends UserEntry> S save(S entity)
    {
        if (StringUtils.isNotBlank(entity.getSourceId()))
        {
            throw new IllegalStateException("Can't modify external users.");
        }

        return super.save(entity);
    }

    public void expireUser(String username, boolean clearSourceId)
    {
        UserEntry externalUserEntry = (UserEntry) detach(findByUsername(username));
        externalUserEntry.setLastUpdate(null);
        if (clearSourceId)
        {
            externalUserEntry.setSourceId("empty");
        }
        entityManager.persist(externalUserEntry);
    }
    
    @Override
    public Class<UserEntry> getEntityClass()
    {
        return UserEntry.class;
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface OrientDb
    {
    }

}
