package org.carlspring.strongbox.event;

import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.event.server.ServerEvent;
import org.carlspring.strongbox.event.server.ServerEventListener;
import org.carlspring.strongbox.event.server.ServerEventListenerRegistry;
import org.carlspring.strongbox.event.server.ServerEventTypeEnum;

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

        ServerEvent artifactEvent = new ServerEvent(ServerEventTypeEnum.EVENT_SERVER_STARTED.getType());

        serverEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyServerEventListener
            implements ServerEventListener
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
