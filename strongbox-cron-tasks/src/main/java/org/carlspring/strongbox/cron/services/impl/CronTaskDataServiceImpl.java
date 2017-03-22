package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.repository.CronTaskConfigurationRepository;
import org.carlspring.strongbox.cron.services.CronTaskDataService;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yougeshwar
 */

@Service
@Transactional
public class CronTaskDataServiceImpl
        implements CronTaskDataService
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskDataService.class);

    @Inject
    CronTaskConfigurationRepository repository;

    @Override
    @Transactional
    public List<CronTaskConfiguration> findByName(String name)
    {
        try
        {
            return repository.findByName(name);
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception: " + e.getMessage(), e);
            return new LinkedList<>();
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CronTaskConfiguration save(CronTaskConfiguration var1)
    {
        return repository.save(var1);
    }

    @Override
    public <S extends CronTaskConfiguration> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public Optional<CronTaskConfiguration> findOne(String var1)
    {
        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    public Optional<List<CronTaskConfiguration>> findAll()
    {
        return Optional.ofNullable(repository.findAll());
    }

    @Override
    public Optional<List<CronTaskConfiguration>> findAll(List<String> var1)
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
    public void delete(CronTaskConfiguration var1)
    {
        repository.delete(var1);
    }

    @Override
    public void delete(Iterable<? extends CronTaskConfiguration> var1)
    {
        repository.delete(var1);
    }

    @Override
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
