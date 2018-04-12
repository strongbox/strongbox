package org.carlspring.strongbox.event.artifact;

import java.nio.file.Path;

import org.carlspring.strongbox.event.EventListener;

/**
 * @author mtodorov
 */
public interface ArtifactEventListener<T extends Path> extends EventListener<ArtifactEvent<T>>
{

}
