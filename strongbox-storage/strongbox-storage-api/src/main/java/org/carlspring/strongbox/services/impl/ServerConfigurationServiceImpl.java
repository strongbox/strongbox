package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ServerConfigurationService;
import org.carlspring.strongbox.storage.repository.ServerConfigurationRepository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    private static final Logger logger = LoggerFactory.getLogger(ServerConfigurationService.class);

    @Autowired
    ServerConfigurationRepository repository;

    @Override
    @Transactional
    public synchronized <S extends Configuration> S save(S var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized <S extends Configuration> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    @Cacheable(value = "configuration", key = "#id")
    public synchronized Optional<Configuration> findOne(String id)
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
    public synchronized Optional<List<Configuration>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    @Transactional
    public synchronized Optional<List<Configuration>> findAll(List<String> var1)
    {
        return Optional.ofNullable(repository.findAll(var1));
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
    public synchronized void delete(Configuration var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(Iterable<? extends Configuration> var1)
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
