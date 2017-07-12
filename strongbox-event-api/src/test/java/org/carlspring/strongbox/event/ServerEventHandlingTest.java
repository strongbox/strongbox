package org.carlspring.strongbox.event;

import org.carlspring.strongbox.config.EventsConfig;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ServerEventHandlingTest
{

    @org.springframework.context.annotation.Configuration
    @Import({ EventsConfig.class })
    public static class SpringConfig
    {
    }

    @Inject
    ServerEventListenerRegistry serverEventListenerRegistry;


    @Test
    public void testEventDispatchingAndHandling()
    {
        DummyServerEventListener listener = new DummyServerEventListener();

        serverEventListenerRegistry.addListener(listener);

        ServerEvent artifactEvent = new ServerEvent(ServerEvent.EVENT_SERVER_STARTED);

        serverEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyServerEventListener
            implements ArtifactEventListener
    {

        boolean eventCaught = false;

        @Override
        public void handle(Event event)
        {
            System.out.println("Caught server event type " + event.getType() + ".");

            eventCaught = true;
        }

    }

}
