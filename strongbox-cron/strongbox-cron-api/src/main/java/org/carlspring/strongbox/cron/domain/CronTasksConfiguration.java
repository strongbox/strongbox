package org.carlspring.strongbox.cron.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "cron-tasks-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class CronTasksConfiguration
{

    @XmlElement(name = "cron-task-configuration")
    private List<CronTaskConfiguration> cronTaskConfigurations = new ArrayList<>();

    public CronTasksConfiguration()
    {
    }

    public CronTasksConfiguration(final List<CronTaskConfiguration> cronTaskConfigurations)
    {
        this.cronTaskConfigurations = cronTaskConfigurations;
    }

    public List<CronTaskConfiguration> getCronTaskConfigurations()
    {
        return cronTaskConfigurations;
    }

    public void setCronTaskConfigurations(List<CronTaskConfiguration> cronTaskConfigurations)
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
