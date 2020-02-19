package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactGroupEntry;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactGroupService<T extends ArtifactGroupEntry>
        extends CrudService<T, String>
{

    void addArtifactToGroup(T artifactGroup,
                            ArtifactEntity artifactEntry);
    
}
