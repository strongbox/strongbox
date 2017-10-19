package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.Event;
import org.carlspring.strongbox.event.CommonEventType;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;

public class RepositorySearchEvent extends Event
{

    private RepositorySearchRequest eventData;

    public RepositorySearchEvent(int type, RepositorySearchRequest data)
    {
        super(CommonEventType.REPOSITORY_SEARCH_EVENT.getTypeId());
        this.eventData = data;
    }

    public RepositorySearchRequest getEventData()
    {
        return eventData;
    }

}
