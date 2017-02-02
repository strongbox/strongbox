package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.repository.AuthorizationConfigRepository;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.service.AuthorizationConfigService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class AuthorizationConfigServiceImpl
        implements AuthorizationConfigService
{

    @Inject
    AuthorizationConfigRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <S extends AuthorizationConfig> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    public <S extends AuthorizationConfig> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    public Optional<AuthorizationConfig> findOne(String var1)
    {
        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    public Optional<List<AuthorizationConfig>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    public Optional<List<AuthorizationConfig>> findAll(List<String> var1)
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
    public void delete(AuthorizationConfig var1)
    {
        repository.delete(var1);
    }

    @Override
    public void delete(Iterable<? extends AuthorizationConfig> var1)
    {
        repository.delete(var1);
    }

    @Override
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
