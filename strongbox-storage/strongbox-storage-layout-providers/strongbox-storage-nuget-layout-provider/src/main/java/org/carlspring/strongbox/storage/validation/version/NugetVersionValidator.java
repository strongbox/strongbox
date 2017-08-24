package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

/**
 * @author Przemyslaw Fusik
 */
interface NugetVersionValidator
        extends VersionValidator
{

    @Override
    default boolean supports(Repository repository)
    {
        return RepositoryLayoutEnum.NUGET_HIERACHLICAL.getLayout().equals(repository.getLayout());
    }
}
