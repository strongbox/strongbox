package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.repository.StrongboxUserRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link StrongboxUser} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class StrongboxUserServiceImpl
        implements StrongboxUserService
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUserService.class);

    @Autowired
    StrongboxUserRepository repository;

    @Override
    @Transactional
    public Optional<StrongboxUser> findByUserName(String username)
    {
        try
        {
            StrongboxUser user = repository.findByUsername(username);
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
    public <S extends StrongboxUser> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    public <S extends StrongboxUser> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public Optional<StrongboxUser> findOne(String var1)
    {
        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    public Optional<Iterable<StrongboxUser>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    public Optional<Iterable<StrongboxUser>> findAll(Iterable<String> var1)
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
    public void delete(StrongboxUser var1)
    {
        repository.delete(var1);
    }

    @Override
    public void delete(Iterable<? extends StrongboxUser> var1)
    {
        repository.delete(var1);
    }

    @Override
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
