package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.ArtifactTag;

/**
 * @author Sergey Bespalov
 *
 */
public interface ArtifactTagService
{

    ArtifactTag findOneOrCreate(String name);

}
