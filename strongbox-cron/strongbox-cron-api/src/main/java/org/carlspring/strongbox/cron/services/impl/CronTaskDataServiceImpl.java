package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.config.CronTasksConfigurationFileManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskUUIDNotUniqueException;
import org.carlspring.strongbox.cron.jobs.CronJobsDefinitionsRegistry;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;

import javax.inject.Inject;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Yougeshwar
 * @author Pablo Tirado
 */
@Service
public class CronTaskDataServiceImpl
        implements CronTaskDataService
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskDataService.class);

    private final ReadWriteLock cronTasksConfigurationLock = new ReentrantReadWriteLock();

    private CronTasksConfigurationFileManager cronTasksConfigurationFileManager;

    private CronJobsDefinitionsRegistry cronJobsDefinitionsRegistry;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #cronTasksConfigurationLock} here
     * and should not be exposed to the world.
     */
    private final CronTasksConfigurationDto configuration;

    @Inject
    public CronTaskDataServiceImpl(CronTasksConfigurationFileManager cronTasksConfigurationFileManager,
                                   CronJobsDefinitionsRegistry cronJobsDefinitionsRegistry) throws IOException
    {
        this.cronTasksConfigurationFileManager = cronTasksConfigurationFileManager;
        this.cronJobsDefinitionsRegistry = cronJobsDefinitionsRegistry;

        CronTasksConfigurationDto cronTasksConfiguration = cronTasksConfigurationFileManager.read();
        for (Iterator<CronTaskConfigurationDto> iterator = cronTasksConfiguration.getCronTaskConfigurations().iterator(); iterator.hasNext(); )
        {
            CronTaskConfigurationDto c = iterator.next();

            logger.debug("Saving cron configuration {}", c);

            String jobClass = c.getJobClass();
            if (jobClass != null && !jobClass.trim().isEmpty())
            {
                try
                {
                    Class.forName(jobClass);
                }
                catch (ClassNotFoundException e)
                {
                    logger.warn(String.format("Skip configuration, job class not found [%s].", jobClass));
                    iterator.remove();
                }
            }

        }

        this.configuration = cronTasksConfiguration;
    }

    @Override
    public CronTasksConfigurationDto getTasksConfigurationDto()
    {
        final Lock readLock = cronTasksConfigurationLock.readLock();
        readLock.lock();

        try
        {
            return ThrowingFunction.unchecked((CronTasksConfigurationDto x) -> SerializationUtils.clone(x)).apply(configuration);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public CronTaskConfigurationDto getTaskConfigurationDto(UUID cronTaskConfigurationUuid)
    {
        final Lock readLock = cronTasksConfigurationLock.readLock();
        readLock.lock();

        try
        {
            Optional<CronTaskConfigurationDto> cronTaskConfiguration = configuration.getCronTaskConfigurations()
                                                                                    .stream()
                                                                                    .filter(conf -> Objects.equals(
                                                                                            cronTaskConfigurationUuid,
                                                                                            conf.getUuid()))
                                                                                    .findFirst();
            return cronTaskConfiguration.map(SerializationUtils::clone).orElse(null);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public List<CronTaskConfiguration> findMatching(CronTaskConfigurationSearchCriteria searchCriteria)
    {
        final Lock readLock = cronTasksConfigurationLock.readLock();
        readLock.lock();

        try
        {
            Stream<CronTaskConfigurationDto> stream = configuration.getCronTaskConfigurations().stream();
            if (!searchCriteria.isEmpty())
            {
                if (!CollectionUtils.isEmpty(searchCriteria.getProperties()))
                {
                    for (Map.Entry<String, String> entry : searchCriteria.getProperties().entrySet())
                    {
                        stream = stream.filter(conf -> entry.getValue()
                                                            .equals(conf.getProperties().get(entry.getKey())));
                    }
                }
            }
            return stream.map(CronTaskConfiguration::new).collect(Collectors.toList());
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public UUID save(final CronTaskConfigurationDto dto) throws IOException
    {
        if (dto.getUuid() == null || StringUtils.isBlank(dto.getUuid().toString()))
        {
            dto.setUuid(UUID.randomUUID());

            if (exists(dto.getUuid()))
            {
                String errorMessage = String.format("Cron task configuration UUID '%s' already exists", dto.getUuid());
                throw new CronTaskUUIDNotUniqueException(errorMessage);
            }
        }

        if (StringUtils.isBlank(dto.getName()))
        {
            cronJobsDefinitionsRegistry.getCronJobDefinitions()
                                       .stream()
                                       .filter(cj -> cj.getJobClass().equals(dto.getJobClass()))
                                       .findFirst()
                                       .map(cj -> {
                                           dto.setName(cj.getName());
                                           return cj;
                                       }).orElseThrow(
                    () -> new IllegalArgumentException(String.format("Unrecognized cron job %s", dto.getJobClass())));
        }

        modifyInLock(configuration ->
                     {
                         configuration.getCronTaskConfigurations()
                                      .stream()
                                      .filter(conf -> Objects.equals(dto.getUuid(), conf.getUuid()))
                                      .findFirst()
                                      .ifPresent(conf -> configuration.getCronTaskConfigurations().remove(conf));


                         configuration.getCronTaskConfigurations().add(dto);
                     });

        return dto.getUuid();
    }

    @Override
    public void delete(final UUID cronTaskConfigurationUuid) throws IOException
    {
        modifyInLock(configuration ->
                             configuration.getCronTaskConfigurations()
                                          .stream()
                                          .filter(conf -> Objects.equals(cronTaskConfigurationUuid, conf.getUuid()))
                                          .findFirst()
                                          .ifPresent(conf -> configuration.getCronTaskConfigurations().remove(conf)));
    }

    private void modifyInLock(final Consumer<CronTasksConfigurationDto> operation) throws IOException
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<CronTasksConfigurationDto> operation,
                              final boolean storeInFile) throws IOException
    {
        final Lock writeLock = cronTasksConfigurationLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(configuration);

            if (storeInFile)
            {
                cronTasksConfigurationFileManager.store(configuration);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private boolean exists(UUID uuid)
    {
        return this.getTaskConfigurationDto(uuid) != null;
    }

}
