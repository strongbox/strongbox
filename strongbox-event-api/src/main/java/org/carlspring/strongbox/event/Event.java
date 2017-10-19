package org.carlspring.strongbox.event;

/**
 * @author carlspring
 */
public class Event
{

    private final int type;


    public Event(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

}
