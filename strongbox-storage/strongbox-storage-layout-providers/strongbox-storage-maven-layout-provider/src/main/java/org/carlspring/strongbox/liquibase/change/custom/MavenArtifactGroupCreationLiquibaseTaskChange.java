package org.carlspring.strongbox.liquibase.change.custom;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactGroupCreationLiquibaseTaskChange
        implements CustomTaskChange
{

    @Override
    public void execute(final Database database)
            throws CustomChangeException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfirmationMessage()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUp()
            throws SetupException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFileOpener(final ResourceAccessor resourceAccessor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValidationErrors validate(final Database database)
    {
        throw new UnsupportedOperationException();
    }
}
