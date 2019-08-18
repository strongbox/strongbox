package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.util.ThrowingFunction;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class CronJobsDefinitionsRegistry
{

    private final Set<CronJobDefinition> cronJobDefinitions;

    CronJobsDefinitionsRegistry(final CronJobsRegistry cronJobsRegistry)
    {
        cronJobDefinitions = cronJobsRegistry.get()
                                             .stream()
                                             .map(ThrowingFunction.unchecked(clazz -> clazz.newInstance()
                                                                                           .getCronJobDefinition()))
                                             .collect(ImmutableSet.toImmutableSet());
    }

    public Set<CronJobDefinition> getCronJobDefinitions()
    {
        return cronJobDefinitions;
    }

    public Optional<CronJobDefinition> get(String id)
    {
        return cronJobDefinitions.stream()
                                 .filter(d -> StringUtils.equals(d.getJobClass(), id))
                                 .findFirst();
    }
}
