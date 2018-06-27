package org.carlspring.strongbox.cron.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "cron-tasks-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class CronTasksConfigurationDto implements Serializable
{

    @XmlElement(name = "cron-task-configuration")
    private Set<CronTaskConfigurationDto> cronTaskConfigurations = new LinkedHashSet<>();

    public CronTasksConfigurationDto()
    {
    }

    public CronTasksConfigurationDto(final Set<CronTaskConfigurationDto> cronTaskConfigurations)
    {
        this.cronTaskConfigurations = cronTaskConfigurations;
    }

    public Set<CronTaskConfigurationDto> getCronTaskConfigurations()
    {
        return cronTaskConfigurations;
    }

    public void setCronTaskConfigurations(Set<CronTaskConfigurationDto> cronTaskConfigurations)
    {
        this.cronTaskConfigurations = cronTaskConfigurations;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CronTasksConfiguration{");
        sb.append("cronTaskConfigurations=").append(cronTaskConfigurations);
        sb.append('}');
        return sb.toString();
    }
}
