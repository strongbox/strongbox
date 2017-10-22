package org.carlspring.strongbox.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.GenericTypeResolver;

/**
 * @author carlspring
 */
public abstract class AbstractEventListenerRegistry
{

    private Map<Class<?>,List<EventListener<?>>> eventListenerMap = new ConcurrentHashMap<>();

    
    public <T extends Event> void dispatchEvent(T event)
    {
    	Class<? extends Event> eventType = event.getClass();
		List<EventListener<?>> eventListenerList = eventListenerMap.get(eventType);
		if (eventListenerList == null) 
		{
			return;
		}
        for (EventListener<?> listener : eventListenerList)
        {
            ((EventListener<T>)listener).handle(event);
        }
    }

    public <T extends Event> void addListener(EventListener<T> listener)
    {
    	Class<T> eventType = (Class<T>) GenericTypeResolver.resolveTypeArgument(listener.getClass(), EventListener.class);
		List<EventListener<?>> eventListenerList = eventListenerMap.get(eventType);
		if (eventListenerList == null) 
		{
			eventListenerList = new LinkedList<>();
			eventListenerMap.putIfAbsent(eventType, eventListenerList);
		}
		eventListenerList.add(listener);
    }

    public <T extends Event> boolean removeListener(EventListener<T> listener)
    {
        return eventListenerMap.values().remove(listener);
    }

}
