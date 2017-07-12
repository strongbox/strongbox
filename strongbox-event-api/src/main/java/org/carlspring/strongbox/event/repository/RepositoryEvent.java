package org.carlspring.strongbox.event.repository;

import org.carlspring.strongbox.event.Event;

/**
 * @author mtodorov
 */
public class RepositoryEvent extends Event
{

    public RepositoryEvent(int type)
    {
        setType(type);
    }

}
