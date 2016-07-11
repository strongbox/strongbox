package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.services.ServerConfigurationService;
import org.carlspring.strongbox.storage.repository.ServerConfigurationRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class ServerConfigurationServiceImpl
        implements ServerConfigurationService
{

    @Autowired
    ServerConfigurationRepository repository;

    @Override
    @Transactional
    public synchronized <S extends BinaryConfiguration> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized <S extends BinaryConfiguration> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized Optional<BinaryConfiguration> findOne(String id)
    {
        return Optional.ofNullable(repository.findOne(id));
    }

    @Override
    @Transactional
    public synchronized boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    @Transactional
    public synchronized Optional<List<BinaryConfiguration>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    @Transactional
    public synchronized Optional<List<BinaryConfiguration>> findAll(List<String> ids)
    {
        return Optional.ofNullable(repository.findAll(ids));
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
    public synchronized void delete(BinaryConfiguration var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(Iterable<? extends BinaryConfiguration> var1)
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
