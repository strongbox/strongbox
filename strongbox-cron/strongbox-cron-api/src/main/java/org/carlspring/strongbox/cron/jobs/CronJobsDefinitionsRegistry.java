package org.carlspring.strongbox.cron.jobs;

import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class CronJobsDefinitionsRegistry
{

    private final Set<CronJobDefinition> cronJobDefinitions;

    public CronJobsDefinitionsRegistry()
    {
        Set<Class<? extends AbstractCronJob>> cronJobs = new Reflections("org.carlspring.strongbox").getSubTypesOf(
                AbstractCronJob.class).stream().filter(
                c -> !Modifier.isAbstract(c.getModifiers()) && !c.isInterface()).collect(Collectors.toSet());

        cronJobDefinitions = cronJobs.stream()
                                     .map(clazz -> {
                                         try
                                         {
                                             return clazz.newInstance().getCronJobDefinition();
                                         }
                                         catch (Exception ex)
                                         {
                                             throw new UndeclaredThrowableException(ex);
                                         }
                                     })
                                     .collect(ImmutableSet.toImmutableSet());
    }

    public Set<CronJobDefinition> getCronJobDefinitions()
    {
        return cronJobDefinitions;
    }

    public Optional<CronJobDefinition> get(String id)
    {
        return cronJobDefinitions.stream()
                                 .filter(d -> d.getJobClass().equals(id))
                                 .findFirst();
    }
}
