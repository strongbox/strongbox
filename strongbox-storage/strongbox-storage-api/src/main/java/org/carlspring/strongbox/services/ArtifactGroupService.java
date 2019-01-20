package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactGroupService<T extends ArtifactGroup>
        extends CrudService<T, String>
{

}
