package org.carlspring.strongbox.event;

public enum CommonEventType
{

    REPOSITORY_SEARCH_EVENT(105);
    
    private final int typeId;

    private CommonEventType(int typeId)
    {
        this.typeId = typeId;
    }

    public int getTypeId()
    {
        return typeId;
    }

}
