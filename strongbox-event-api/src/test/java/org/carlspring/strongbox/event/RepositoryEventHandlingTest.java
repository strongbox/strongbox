package org.carlspring.strongbox.event;

import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListener;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryEventHandlingTest
{

    @org.springframework.context.annotation.Configuration
    @Import({ EventsConfig.class })
    public static class SpringConfig
    {
    }

    @Inject
    RepositoryEventListenerRegistry repositoryEventListenerRegistry;


    @Test
    public void testEventDispatchingAndHandling()
    {
        DummyRepositoryEventListener listener = new DummyRepositoryEventListener();

        repositoryEventListenerRegistry.addListener(listener);

        RepositoryEvent artifactEvent = new RepositoryEvent("storage0",
                                                            "releases",
                                                            RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_IN_SERVICE.getType());

        repositoryEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyRepositoryEventListener
            implements RepositoryEventListener
    {

        boolean eventCaught = false;

        @Override
        public void handle(RepositoryEvent event)
        {
            System.out.println("Caught repository event type " + event.getType() + " for " +
                               event.getStorageId() + ":" + event.getRepositoryId() + ".");

            eventCaught = true;
        }

    }

}
