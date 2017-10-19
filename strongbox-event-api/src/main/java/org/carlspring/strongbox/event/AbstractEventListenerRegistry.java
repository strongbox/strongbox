package org.carlspring.strongbox.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author carlspring
 */
public abstract class AbstractEventListenerRegistry
{

    private List<EventListener<?>> eventListeners = new ArrayList<>();

    
    public <T extends Event> void dispatchEvent(T event)
    {
        for (EventListener<?> listener : eventListeners)
        {
            ((EventListener<T>)listener).handle(event);
        }
    }

    public <T extends Event> void addListener(EventListener<T> listener)
    {
        eventListeners.add(listener);
    }

    public <T extends Event> boolean removeListener(EventListener<T> listener)
    {
        return eventListeners.remove(listener);
    }

    public List<EventListener<?>> getEventListeners()
    {
        return eventListeners;
    }

    public void setEventListeners(List<EventListener<?>> eventListeners)
    {
        this.eventListeners = eventListeners;
    }

}
