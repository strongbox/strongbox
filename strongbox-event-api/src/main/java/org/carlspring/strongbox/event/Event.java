package org.carlspring.strongbox.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author carlspring
 */
public class Event extends ApplicationEvent
{

    public Event(Object source)
    {
        super(source);
    }


    public int getType()
    {
        return (int) getSource();
    }

}
