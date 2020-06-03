package org.carlspring.strongbox.services;

import java.util.Set;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactGroupService<T extends ArtifactGroup>
{

    void saveArtifacts(Repository repository,
                       Set<Artifact> artifactToSaveSet);

    ArtifactCoordinates addArtifactToGroup(T artifactGroup,
                                           Artifact artifactEntry);

}
