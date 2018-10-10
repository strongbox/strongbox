package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.config.CronTasksConfigurationFileManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskConfigurationException;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Yougeshwar
 */
@Service
public class CronTaskDataServiceImpl
        implements CronTaskDataService
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskDataService.class);

    private final ReadWriteLock cronTasksConfigurationLock = new ReentrantReadWriteLock();

    @Inject
    private CronTasksConfigurationFileManager cronTasksConfigurationFileManager;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #cronTasksConfigurationLock} here
     * and should not be exposed to the world.
     */
    private final CronTasksConfigurationDto configuration = new CronTasksConfigurationDto();

    @Override
    public CronTasksConfigurationDto getTasksConfigurationDto()
    {
        final Lock readLock = cronTasksConfigurationLock.readLock();
        readLock.lock();

        try
        {
            return SerializationUtils.clone(configuration);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public CronTaskConfigurationDto getTaskConfigurationDto(String cronTaskConfigurationName)
    {
        final Lock readLock = cronTasksConfigurationLock.readLock();
        readLock.lock();

        try
        {
            Optional<CronTaskConfigurationDto> cronTaskConfiguration = configuration.getCronTaskConfigurations()
                                                                                    .stream()
                                                                                    .filter(conf -> cronTaskConfigurationName.equals(conf.getName()))
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
    public void save(final CronTaskConfigurationDto dto)
    {
        if (StringUtils.isBlank(dto.getName()))
        {
            throw new CronTaskConfigurationException(String.format("Cron task name '%s' should not be blank",
                                                                   dto.getName()));
        }
        modifyInLock(configuration ->
                     {
                         configuration.getCronTaskConfigurations()
                                      .stream()
                                      .filter(conf -> StringUtils.equals(dto.getName(), conf.getName()))
                                      .findFirst()
                                      .ifPresent(conf -> configuration.getCronTaskConfigurations().remove(conf));
                         configuration.getCronTaskConfigurations().add(dto);
                     });
    }

    @Override
    public void delete(final String name)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getCronTaskConfigurations()
                                      .stream()
                                      .filter(conf -> name.equals(conf.getName()))
                                      .findFirst()
                                      .ifPresent(conf -> configuration.getCronTaskConfigurations().remove(conf));
                     });
    }

    private void modifyInLock(final Consumer<CronTasksConfigurationDto> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<CronTasksConfigurationDto> operation,
                              final boolean storeInFile)
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

}
