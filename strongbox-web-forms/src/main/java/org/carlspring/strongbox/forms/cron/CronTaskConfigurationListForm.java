package org.carlspring.strongbox.forms.cron;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Pablo Tirado
 */
public class CronTaskConfigurationListForm
        implements Serializable
{

    @Valid
    private Set<CronTaskConfigurationForm> cronTaskConfigurations;


    public Set<CronTaskConfigurationForm> getCronTaskConfigurations()
    {
        return cronTaskConfigurations;
    }

    public void setCronTaskConfigurations(Set<CronTaskConfigurationForm> cronTaskConfigurations)
    {
        this.cronTaskConfigurations = cronTaskConfigurations;
    }
}
