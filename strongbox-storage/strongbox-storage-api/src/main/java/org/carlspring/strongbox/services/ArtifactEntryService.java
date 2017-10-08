package org.carlspring.strongbox.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
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

    /**
     * Returns list of artifacts that matches search query defined as {@link ArtifactCoordinates} fields.
     * By default all fields are optional and combined using logical AND operator.
     * If all coordinates aren't present this query will delegate request to {@link #findAll()}
     * (because in that case every ArtifactEntry will match the query).
     *
     * @param coordinates search query defined as a set of coordinates (id ,version, groupID etc.)
     * @return list of artifacts or empty list if nothing was found
     */
    List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates);
    
    List<ArtifactEntry> findByCoordinates(Map<String, String> coordinates);
    
    List<ArtifactEntry> findByCoordinates(Map<String, String> coordinates, String orderBy, boolean strict);
    
    Optional<ArtifactEntry> findOne(ArtifactCoordinates artifactCoordinates);
    
    boolean existsByCoordinates(String storageId, String repositoryId, ArtifactCoordinates c);
}
