package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.Event;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;

public class RemoteRepositorySearchEvent extends Event
{

    private RepositorySearchRequest eventData;

    public RemoteRepositorySearchEvent(RepositorySearchRequest data)
    {
        super(-1);
        this.eventData = data;
    }

    public RepositorySearchRequest getEventData()
    {
        return eventData;
    }

}
