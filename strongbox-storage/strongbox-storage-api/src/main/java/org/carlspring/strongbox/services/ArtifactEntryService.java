package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;

import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service for managing {@link ArtifactEntry} entities.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface ArtifactEntryService
        extends CrudService<ArtifactEntry, String>
{

    @Transactional
    ArtifactEntry findByCoordinates(final String username);

}
