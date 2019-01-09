package org.carlspring.strongbox.validation.cron;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Pablo Tirado
 */
public class UniqueCronTaskConfigurationValidator
        implements ConstraintValidator<UniqueCronTaskConfiguration, String>
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Override
    public void initialize(UniqueCronTaskConfiguration constraint)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(String uuid,
                           ConstraintValidatorContext context)
    {
        CronTaskConfigurationDto configuration = cronTaskConfigurationService.getTaskConfigurationDto(uuid);
        return configuration == null;
    }

}
