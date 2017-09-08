package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

/**
 * @author Przemyslaw Fusik
 */
interface MavenVersionValidator
        extends VersionValidator
{

    @Override
    default boolean supports(Repository repository)
    {
        return RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout());
    }
}
