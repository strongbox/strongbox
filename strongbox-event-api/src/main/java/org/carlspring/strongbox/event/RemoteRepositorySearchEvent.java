package org.carlspring.strongbox.event;

public class RemoteRepositorySearchEvent extends Event
{

    public RemoteRepositorySearchEvent(int type)
    {
        super(SbEventType.REMOTE_REPOSITORY_SEARCH_EVENT.getTypeId());
    }
    
}
