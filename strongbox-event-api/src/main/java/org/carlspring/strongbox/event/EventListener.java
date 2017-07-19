package org.carlspring.strongbox.event;

/**
 * @author carlspring
 */
public interface EventListener<T extends Event>
{

    void handle(T event);

}
