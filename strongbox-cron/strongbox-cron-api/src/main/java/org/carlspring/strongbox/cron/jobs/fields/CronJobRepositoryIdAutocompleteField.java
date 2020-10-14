package org.carlspring.strongbox.cron.jobs.fields;

import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;

import javax.annotation.concurrent.Immutable;

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
        return REPOSITORY_ID;
    }
}
