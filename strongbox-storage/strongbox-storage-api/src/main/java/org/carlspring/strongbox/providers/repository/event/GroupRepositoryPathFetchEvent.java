package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.RepositoryBasedEvent;
import org.carlspring.strongbox.providers.io.RepositoryPath;

/**
 * @author Przemyslaw Fusik
 */
public class GroupRepositoryPathFetchEvent
        extends RepositoryBasedEvent<RepositoryPath>
{
    public GroupRepositoryPathFetchEvent(final RepositoryPath path)
    {
        super(path, -1);
    }
}
