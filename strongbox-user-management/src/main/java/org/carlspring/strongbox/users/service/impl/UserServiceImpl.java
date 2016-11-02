package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.repository.UserRepository;
import org.carlspring.strongbox.users.service.UserService;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    UserRepository repository;

    @Autowired
    CacheManager cacheManager;

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#name", sync = true)
    public synchronized User findByUserName(String name)
    {
        try
        {
            return repository.findByUsername(name);
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception: " + e.getLocalizedMessage());
            logger.trace("Exception details: ", e);

            return null;
        }
    }

    @Override
    @Transactional
    public synchronized <S extends User> S save(S var1)
    {
        // ID non-null check was removed because there will be no ID assigned by database
        // until transaction is not committed (depends on PROPAGATE value)
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized <S extends User> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized Optional<User> findOne(String var1)
    {
        if (var1 == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    @Transactional
    public synchronized boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
    public synchronized long count()
    {
        return repository.count();
    }

    @Override
    @Transactional
    public synchronized void delete(String var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(User var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(Iterable<? extends User> var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void deleteAll()
    {
        repository.deleteAll();
    }
}
