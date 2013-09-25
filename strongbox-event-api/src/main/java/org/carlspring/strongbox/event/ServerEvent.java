package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class ServerEvent
{

    public static final int EVENT_SERVER_STARTED = 1;

    public static final int EVENT_SERVER_STOPPED = 2;

    public static final int EVENT_SERVER_CONFIGURATION_CHANGED = 3;

    private int type;


    public ServerEvent()
    {
    }

    public ServerEvent(int type)
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
