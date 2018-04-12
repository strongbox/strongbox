package org.carlspring.strongbox.event;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

        Path path = Paths.get("storage0",
                              "releases",
                              "foo/bar/1.2.3/bar-1.2.3.jar");
        ArtifactEvent<Path> artifactEvent = new ArtifactEvent<>(path,
                ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPLOADED.getType());

        artifactEventListenerRegistry.dispatchEvent(artifactEvent);

        assertEquals("Failed to catch event!", true, listener.eventCaught);
    }

    private class DummyArtifactEventListener
            implements ArtifactEventListener<Path>
    {

        boolean eventCaught = false;

        @Override
        public void handle(ArtifactEvent<Path> event)
        {
            System.out.println("Caught artifact event type " + event.getType() + ".");

            eventCaught = true;
        }

    }

}
