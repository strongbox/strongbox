package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.Event;

/**
 * @author mtodorov
 */
public class ArtifactEvent extends Event
{

    public ArtifactEvent(int type)
    {
        setType(type);
    }

}
