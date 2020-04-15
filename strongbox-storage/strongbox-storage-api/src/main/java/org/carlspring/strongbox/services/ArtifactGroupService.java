package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroup;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactGroupService<T extends ArtifactGroup>
{

    ArtifactCoordinates addArtifactToGroup(T artifactGroup,
                                           Artifact artifactEntry);

    ArtifactIdGroup findOneOrCreate(String storageId,
                                    String repositoryId,
                                    String artifactId);
}
