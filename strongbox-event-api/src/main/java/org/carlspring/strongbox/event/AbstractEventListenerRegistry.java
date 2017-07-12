package org.carlspring.strongbox.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author carlspring
 */
public abstract class AbstractEventListenerRegistry<T extends Event>
{

    private List<EventListener> eventListeners = new ArrayList<>();

    
    public void dispatchEvent(T event)
    {
        for (EventListener listener : eventListeners)
        {
            listener.handle(event);
        }
    }

    public void addListener(EventListener listener)
    {
        eventListeners.add(listener);
    }

    public List<EventListener> getEventListeners()
    {
        return eventListeners;
    }

    public void setEventListeners(List<EventListener> eventListeners)
    {
        this.eventListeners = eventListeners;
    }

}
