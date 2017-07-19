package org.carlspring.strongbox.event.repository;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RepositoryEventListenerRegistry
        extends AbstractEventListenerRegistry<RepositoryEvent>
{

}
