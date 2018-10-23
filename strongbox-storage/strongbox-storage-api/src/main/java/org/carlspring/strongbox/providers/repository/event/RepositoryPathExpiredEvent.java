package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.RepositoryBasedEvent;
import org.carlspring.strongbox.providers.io.RepositoryPath;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryPathExpiredEvent
        extends RepositoryBasedEvent<RepositoryPath>
{


    public RepositoryPathExpiredEvent(final RepositoryPath path)
    {
        super(path, -1);
    }
}
