package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.repository.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactEntryService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orientechnologies.orient.core.query.OQueryAbstract;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    ArtifactRepository repository;

    @Inject
    CacheManager cacheManager;

    @Inject
    OObjectDatabaseTx databaseTx;

    @Override
    @SuppressWarnings("unchecked")
    // don't try to use second level cache here until you make all coordinates properly serializable
    public List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates)
    {
        // if search query is null or empty delegate to #findAll
        if (coordinates == null || coordinates.getCoordinates()
                                              .keySet()
                                              .isEmpty())
        {
            return findAll().orElse(Collections.EMPTY_LIST);
        }

        // prepare custom query based on all non-null coordinates that were joined by logical AND
        // read more about fetching strategies here: http://orientdb.com/docs/2.2/Fetching-Strategies.html
        String nativeQuery = buildQueryFrom(coordinates);
        OQueryAbstract query = new OSQLSynchQuery<>(nativeQuery).setFetchPlan("*:-1");

        logger.info("[findByCoordinates] SQL -> \n\t" + nativeQuery);

        databaseTx.activateOnCurrentThread();
        List<ArtifactEntry> resultList = databaseTx.query(query);

        // still have to detach everything manually until we fully migrate to OrientDB 2.2.X
        // where fetching strategies will be fully supported
        List<ArtifactEntry> detachedList = new LinkedList<>();
        resultList.forEach(artifactEntry -> detachedList.add(databaseTx.detachAll(artifactEntry, true)));

        return detachedList;
    }

    private String buildQueryFrom(ArtifactCoordinates coordinates)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ")
          .append(ARTIFACT_ENTRY_CLASS_NAME);

        Map<String, String> map = coordinates.getCoordinates();

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
        return repository.save(var1);
    }

    @Override
    public <S extends ArtifactEntry> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public Optional<ArtifactEntry> findOne(String var1)
    {
        if (var1 == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    @Transactional
    public boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    @Transactional
    public Optional<List<ArtifactEntry>> findAll()
    {
        try
        {
            return Optional.ofNullable(repository.findAll());
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
            return Optional.ofNullable(repository.findAll(var1));
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
        return repository.count();
    }

    @Override
    @Transactional
    public void delete(String var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(ArtifactEntry var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void delete(Iterable<? extends ArtifactEntry> var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public void deleteAll()
    {
        repository.deleteAll();
    }
}
