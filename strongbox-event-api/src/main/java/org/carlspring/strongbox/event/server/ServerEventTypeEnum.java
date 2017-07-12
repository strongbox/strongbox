package org.carlspring.strongbox.event.server;

/**
 * @author carlspring
 */
public enum ServerEventTypeEnum
{

    EVENT_SERVER_INITIALIZING(1),

    EVENT_SERVER_STARTED(2),

    EVENT_SERVER_STOPPED(3),

    EVENT_SERVER_CONFIGURATION_CHANGED(4);

    private int type;


    ServerEventTypeEnum(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

}
