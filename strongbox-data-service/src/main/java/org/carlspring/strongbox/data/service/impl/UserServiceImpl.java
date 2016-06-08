package org.carlspring.strongbox.data.service.impl;

import org.carlspring.strongbox.data.domain.User;
import org.carlspring.strongbox.data.repository.UserRepository;

import java.util.Optional;

import org.carlspring.strongbox.data.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional
    public Optional<User> findByUserName(String username)
    {
        try
        {
            User user = repository.findByUsername(username);
            return Optional.ofNullable(user);
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public <S extends User> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    public <S extends User> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public Optional<User> findOne(String var1)
    {
        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    public Optional<Iterable<User>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    public Optional<Iterable<User>> findAll(Iterable<String> var1)
    {
        return Optional.ofNullable(repository.findAll(var1));
    }

    @Override
    public long count()
    {
        return repository.count();
    }

    @Override
    public void delete(String var1)
    {
        repository.delete(var1);
    }

    @Override
    public void delete(User var1)
    {
        repository.delete(var1);
    }

    @Override
    public void delete(Iterable<? extends User> var1)
    {
        repository.delete(var1);
    }

    @Override
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
