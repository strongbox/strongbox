package org.carlspring.strongbox.cron.jobs;

import com.google.common.collect.ImmutableSet;
import org.carlspring.strongbox.util.ThrowingFunction;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
                                     .map(ThrowingFunction.unchecked(clazz -> clazz.newInstance().getCronJobDefinition()))
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
