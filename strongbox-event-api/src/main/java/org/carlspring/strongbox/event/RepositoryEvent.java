package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class RepositoryEvent extends Event
{

    public static final int EVENT_REPOSITORY_CREATED = 1;

    public static final int EVENT_REPOSITORY_CHANGED = 2;

    public static final int EVENT_REPOSITORY_DELETED = 3;

    public static final int EVENT_REPOSITORY_PUT_IN_SERVICE = 4;

    public static final int EVENT_REPOSITORY_PUT_OUT_OF_SERVICE = 5;

    public static final int EVENT_REPOSITORY_REMOTE_UNAVAILABLE = 6;

    public static final int EVENT_REPOSITORY_REMOTE_AVAILABLE = 7;


    public RepositoryEvent(int type)
    {
        setType(type);
    }

}
