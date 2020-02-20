package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactTagEntity;

/**
 * @author Sergey Bespalov
 *
 */
public interface ArtifactTagService extends CrudService<ArtifactTagEntity, String>
{

    ArtifactTag findOneOrCreate(String name);
    
}
