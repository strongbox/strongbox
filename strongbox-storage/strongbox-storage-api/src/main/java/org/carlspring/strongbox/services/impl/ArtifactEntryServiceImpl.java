package org.carlspring.strongbox.services.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.repository.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * DAO implementation for {@link ArtifactEntry} entities.
 *
 * @author Alex Oreshkevich
 */
@Service
@Transactional
class ArtifactEntryServiceImpl
        implements ArtifactEntryService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryService.class);

    // will help us avoid to have hardcoded name of this class
    private static final String ARTIFACT_ENTRY_CLASS_NAME = ArtifactEntry.class.getSimpleName();

    @Inject
    ArtifactRepository artifactRepository;

    @Inject
    CacheManager cacheManager;

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

        List<ArtifactEntry> resultList = ((OObjectDatabaseTx)entityManager.getDelegate()).query(query);

        // still have to detach everything manually until we fully migrate to OrientDB 2.2.X
        // where fetching strategies will be fully supported
        List<ArtifactEntry> detachedList = new LinkedList<>();
        resultList.forEach(artifactEntry -> detachedList.add(((OObjectDatabaseTx)entityManager.getDelegate()).detachAll(artifactEntry, true)));

        return detachedList;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <S extends ArtifactEntry> S save(S var1)
    {
        return artifactRepository.save(var1);
    }

    @Override
    public <S extends ArtifactEntry> Iterable<S> save(Iterable<S> var1)
    {
        return artifactRepository.save(var1);
    }

    @Override
    @Transactional
    public Optional<ArtifactEntry> findOne(String var1)
    {
        if (var1 == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(artifactRepository.findOne(var1));
    }

    @Override
    public Optional<ArtifactEntry> findOne(ArtifactCoordinates artifactCoordinates)
    {
        List<ArtifactEntry> artifactEntryList = findByCoordinates(artifactCoordinates);
        return Optional.ofNullable(artifactEntryList == null || artifactEntryList.isEmpty() ? null
                : artifactEntryList.iterator().next());
    }

    @Override
    @Transactional
    public boolean exists(String var1)
    {
        return artifactRepository.exists(var1);
    }

    @Override
    @Transactional
    public Optional<List<ArtifactEntry>> findAll()
    {
        try
        {
            return Optional.ofNullable(artifactRepository.findAll());
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception.", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<List<ArtifactEntry>> findAll(List<String> var1)
    {
        try
        {
            return Optional.ofNullable(artifactRepository.findAll(var1));
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception.", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public long count()
    {
        return artifactRepository.count();
    }

    @Override
    @Transactional
    public void delete(String var1)
    {
        artifactRepository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(ArtifactEntry var1)
    {
        artifactRepository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(Iterable<? extends ArtifactEntry> var1)
    {
        artifactRepository.delete(var1);
    }

    @Override
    @Transactional
    public void deleteAll()
    {
        artifactRepository.deleteAll();
    }
}
