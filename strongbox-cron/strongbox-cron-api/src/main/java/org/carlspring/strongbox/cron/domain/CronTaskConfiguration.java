package org.carlspring.strongbox.cron.domain;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronTaskConfiguration
{

    private final String uuid;

    private final String name;

    private final Map<String, String> properties;

    private final boolean oneTimeExecution;

    private final boolean immediateExecution;

    public CronTaskConfiguration(final CronTaskConfigurationDto source)
    {
        this.uuid = source.getUuid();
        this.name = source.getName();
        this.properties = immuteProperties(source.getProperties());
        this.oneTimeExecution = source.isOneTimeExecution();
        this.immediateExecution = source.shouldExecuteImmediately();
    }

    private Map<String, String> immuteProperties(final Map<String, String> source)
    {
        return source != null ? ImmutableMap.copyOf(source) : Collections.emptyMap();
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public boolean isOneTimeExecution()
    {
        return oneTimeExecution;
    }

    public String getProperty(String key)
    {
        return this.properties.get(key);
    }

    public boolean contains(String key)
    {
        return properties.containsKey(key);
    }

    public boolean shouldExecuteImmediately()
    {
        return immediateExecution;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CronTaskConfiguration))
        {
            return false;
        }
        final CronTaskConfiguration that = (CronTaskConfiguration) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uuid);
    }
}
