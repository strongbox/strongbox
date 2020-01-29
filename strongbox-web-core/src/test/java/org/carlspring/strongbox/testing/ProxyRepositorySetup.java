package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;

public class ProxyRepositorySetup
        implements RepositorySetup
{

    @Override
    public void setup(RepositoryDto repository)
    {
        repository.setType(RepositoryTypeEnum.PROXY.getType());
    }

}
