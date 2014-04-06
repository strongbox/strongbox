package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

/**
 * @author stodorov
 */
public class RepositoryGroupValidator
{

    public boolean validate(Repository repository)
    {
        return repository.getType().equals(RepositoryTypeEnum.GROUP.toString());
    }

}
