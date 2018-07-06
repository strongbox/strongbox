package org.carlspring.strongbox.event;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;

/**
 * @author carlspring
 */
public abstract class AbstractEventListenerRegistry
{

    @Inject
    private ApplicationEventPublisher eventPublisher;

    public <T extends Event> void dispatchEvent(T event)
    {
        eventPublisher.publishEvent(event);
    }

}
