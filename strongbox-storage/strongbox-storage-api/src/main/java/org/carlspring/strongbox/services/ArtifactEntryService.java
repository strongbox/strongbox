package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;

import java.util.List;

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

    /**
     * Returns list of artifacts that matches search query defined as {@link ArtifactCoordinates} fields.
     * By default all fields are optional and combined using logical AND operator.
     * If all coordinates aren't present this query will delegate request to {@link #findAll()}.
     *
     * @param coordinates search query defined as a set of coordinates (id ,version, groupID etc.)
     * @return list of artifacts or empty list if nothing was found
     */
    List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates);
}
