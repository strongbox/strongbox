package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.services.ServerConfigurationService;
import org.carlspring.strongbox.storage.repository.ServerConfigurationRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class ServerConfigurationServiceImpl
        implements ServerConfigurationService
{

    @Inject
    ServerConfigurationRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <S extends BinaryConfiguration> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public <S extends BinaryConfiguration> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public Optional<BinaryConfiguration> findOne(String id)
    {
        return Optional.ofNullable(repository.findOne(id));
    }

    @Override
    @Transactional
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    @Transactional
    public Optional<List<BinaryConfiguration>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    @Transactional
    public Optional<List<BinaryConfiguration>> findAll(List<String> ids)
    {
        return Optional.ofNullable(repository.findAll(ids));
    }

    @Override
    @Transactional
    public long count()
    {
        return repository.count();
    }

    @Override
    @Transactional
    public void delete(String var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(BinaryConfiguration var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(Iterable<? extends BinaryConfiguration> var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
