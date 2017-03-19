package org.carlspring.strongbox.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
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
class ArtifactEntryServiceImpl extends CommonCrudService<ArtifactEntry> implements ArtifactEntryService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryService.class);

    // will help us avoid to have hardcoded name of this class
    private static final String ARTIFACT_ENTRY_CLASS_NAME = ArtifactEntry.class.getSimpleName();

    @Override
    public Class<ArtifactEntry> getEntityClass()
    {
        return ArtifactEntry.class;
    }

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public List<ArtifactEntry> findByCoordinates(Map<String, String> coordinates)
    {
        if (coordinates == null || coordinates.keySet()
                                              .isEmpty())
        {
            return findAll().orElse(Collections.EMPTY_LIST);
        }

        // prepare custom query based on all non-null coordinates that were joined by logical AND
        // read more about fetching strategies here: http://orientdb.com/docs/2.2/Fetching-Strategies.html

        String nativeQuery = buildQuery(coordinates);
        OSQLSynchQuery<ArtifactEntry> query = new OSQLSynchQuery<>(nativeQuery);
        logger.info("[findByCoordinates] SQL -> \n\t" + nativeQuery);

        return getDelegate().query(query);
    }

    @Override
    @SuppressWarnings("unchecked")
    // don't try to use second level cache here until you make all coordinates properly serializable
    public List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates)
    {
        return findByCoordinates(coordinates == null ? null : coordinates.getCoordinates());
    }

    private String buildQuery(Map<String, String> map)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ")
          .append(ARTIFACT_ENTRY_CLASS_NAME);

        if (map == null || map.isEmpty())
        {
            return sb.toString();
        }

        sb.append(" where ");

        // process only coordinates with non-null values
        // don't forget to 'wrap' values into ''
        map.entrySet()
           .stream()
           .filter(entry -> entry.getValue() != null)
           .forEach(entry -> sb.append("artifactCoordinates.")
                               .append(entry.getKey())
                               .append(" = '")
                               .append(entry.getValue())
                               .append("' and "));

        // remove last 'and' statement (that don't relates to any coordinate)
        String query = sb.toString();
        query = query.substring(0, query.length() - 5);

        // now query should looks like
        // select * from ArtifactEntry where artifactCoordinates.groupId = ? and ....
        return query + ";";
    }

    @Override
    public Optional<ArtifactEntry> findOne(ArtifactCoordinates artifactCoordinates)
    {
        List<ArtifactEntry> artifactEntryList = findByCoordinates(artifactCoordinates);
        return Optional.ofNullable(artifactEntryList == null || artifactEntryList.isEmpty() ? null
                : artifactEntryList.iterator().next());
    }

}
