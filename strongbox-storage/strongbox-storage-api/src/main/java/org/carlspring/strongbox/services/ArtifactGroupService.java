package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;

import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public interface ArtifactGroupService
        extends CrudService<ArtifactGroup, String>
{

    <T extends ArtifactGroup> T findOneOrCreate(Class<T> type,
                                                Map<String, ? extends Object> properties);
}
