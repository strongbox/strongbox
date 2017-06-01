package org.carlspring.strongbox.services.impl;

import java.util.*;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * DAO implementation for {@link ArtifactEntry} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class ArtifactEntryServiceImpl extends CommonCrudService<ArtifactEntry>
        implements ArtifactEntryService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryService.class);


    @Override
    @Transactional
    public List<ArtifactEntry> findByCoordinates(Map<String, String> coordinates)
    {
        if (coordinates == null || coordinates.keySet().isEmpty())
        {
            return findAll().orElse(Collections.EMPTY_LIST);
        }

        // Prepare a custom query based on all non-null coordinates that were joined by logical AND.
        // Read more about fetching strategies here: http://orientdb.com/docs/2.2/Fetching-Strategies.html

        String nativeQuery = buildQuery(coordinates);
        OSQLSynchQuery<ArtifactEntry> query = new OSQLSynchQuery<>(nativeQuery);

        List<ArtifactEntry> results = getDelegate().command(query).execute(coordinates);

        logger.debug("Executing SQL query> " + nativeQuery);

        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    // don't try to use second level cache here until you make all coordinates properly serializable
    public List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates)
    {
        return findByCoordinates(coordinates == null ? null : coordinates.getCoordinates());
    }

    @Override
    public Optional<ArtifactEntry> findOne(ArtifactCoordinates artifactCoordinates)
    {
        List<ArtifactEntry> artifactEntryList = findByCoordinates(artifactCoordinates);

        return Optional.ofNullable(artifactEntryList == null || artifactEntryList.isEmpty() ?
                                   null : artifactEntryList.iterator().next());
    }

    @Override
    public Class<ArtifactEntry> getEntityClass()
    {
        return ArtifactEntry.class;
    }

}
