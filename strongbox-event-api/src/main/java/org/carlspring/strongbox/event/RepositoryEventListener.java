package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public interface RepositoryEventListener
{

    void handle(RepositoryEvent event);

}
