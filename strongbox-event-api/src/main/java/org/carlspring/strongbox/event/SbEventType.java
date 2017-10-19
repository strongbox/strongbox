package org.carlspring.strongbox.event;

public enum SbEventType
{

    REMOTE_REPOSITORY_SEARCH_EVENT(105);
    
    private final int typeId;

    private SbEventType(int typeId)
    {
        this.typeId = typeId;
    }

    protected int getTypeId()
    {
        return typeId;
    }

}
