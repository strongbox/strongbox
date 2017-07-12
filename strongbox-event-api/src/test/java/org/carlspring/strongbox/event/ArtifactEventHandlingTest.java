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
public class ArtifactEventHandlingTest
{

    @org.springframework.context.annotation.Configuration
    @Import({ EventsConfig.class })
    public static class SpringConfig
    {
    }

    @Inject
    ArtifactEventListenerRegistry artifactEventListenerRegistry;


    @Test
    public void testEventDispatchingAndHandling()
    {
        DummyArtifactEventListener listener = new DummyArtifactEventListener();

        artifactEventListenerRegistry.addListener(listener);

        ArtifactEvent artifactEvent = new ArtifactEvent(ArtifactEvent.EVENT_ARTIFACT_UPLOADED);

        artifactEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyArtifactEventListener implements ArtifactEventListener
    {

        boolean eventCaught = false;

        @Override
        public void handle(Event event)
        {
            System.out.println("Caught artifact event type " + event.getType() + ".");

            eventCaught = true;
        }

    }

}
