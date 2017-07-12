package org.carlspring.strongbox.event;

import org.carlspring.strongbox.config.EventsConfig;

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

        RepositoryEvent artifactEvent = new RepositoryEvent(RepositoryEvent.EVENT_REPOSITORY_PUT_IN_SERVICE);

        repositoryEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyRepositoryEventListener
            implements ArtifactEventListener
    {

        boolean eventCaught = false;

        @Override
        public void handle(Event event)
        {
            System.out.println("Caught repository event type " + event.getType() + ".");

            eventCaught = true;
        }

    }

}
