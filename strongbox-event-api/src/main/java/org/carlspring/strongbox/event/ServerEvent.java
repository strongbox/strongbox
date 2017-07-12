package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class ServerEvent extends Event
{

    public static final int EVENT_SERVER_INITIALIZING = 1;

    public static final int EVENT_SERVER_STARTED = 2;

    public static final int EVENT_SERVER_STOPPED = 3;

    public static final int EVENT_SERVER_CONFIGURATION_CHANGED = 4;


    public ServerEvent(int type)
    {
        setType(type);
    }

}
