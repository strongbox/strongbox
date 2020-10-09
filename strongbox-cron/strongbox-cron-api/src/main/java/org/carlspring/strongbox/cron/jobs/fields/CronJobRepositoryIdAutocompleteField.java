package org.carlspring.strongbox.cron.jobs.fields;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.db.schema.Properties;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronJobRepositoryIdAutocompleteField
        extends CronJobAutocompleteField
{
    public CronJobRepositoryIdAutocompleteField()
    {
        this(null);
    }

    public CronJobRepositoryIdAutocompleteField(CronJobField field)
    {
        super(field);
    }

    @Override
    public String getValue()
    {
        return Properties.REPOSITORY_ID;
    }
}
