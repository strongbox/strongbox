package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class RepositoryEvent
{

    public static final int EVENT_REPOSITORY_CREATED = 1;

    public static final int EVENT_REPOSITORY_CHANGED = 2;

    public static final int EVENT_REPOSITORY_DELETED = 3;

    public static final int EVENT_REPOSITORY_PUT_OUT_OF_SERVICE = 4;

    public static final int EVENT_REPOSITORY_PUT_IN_OF_SERVICE = 5;

    public static final int EVENT_REPOSITORY_REMOTE_UNAVAILABLE = 6;

    public static final int EVENT_REPOSITORY_REMOTE_AVAILABLE = 7;

    private int type;


    public RepositoryEvent()
    {
    }

    public RepositoryEvent(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

}
