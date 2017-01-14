package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.repository.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactEntryService;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
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

    @Autowired
    ArtifactRepository repository;

    @Autowired
    CacheManager cacheManager;

    @Override
    // @Cacheable(value = "artifacts", key = "#coordinates", sync = true)
    public List<ArtifactEntry> findByCoordinates(ArtifactCoordinates coordinates)
    {
        // if search query is null or empty delegate to #findAll
        if (coordinates == null || coordinates.getCoordinates()
                                              .keySet()
                                              .isEmpty())
        {
            throw new UnsupportedOperationException();
            //return findAll().orElse(new LinkedList<>());
        }


        return repository.findByArtifactCoordinates(((MavenArtifactCoordinates) coordinates).getGroupId());
    }


    @Override
    @Transactional
    public synchronized <S extends ArtifactEntry> S save(S var1)
    {
        // ID non-null check was removed because there will be no ID assigned by database
        // until transaction is not committed (depends on PROPAGATE value)
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized <S extends ArtifactEntry> Iterable<S> save(Iterable<S> var1)
    {
        return repository.save(var1);
    }

    @Override
    @Transactional
    public synchronized Optional<ArtifactEntry> findOne(String var1)
    {
        if (var1 == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(repository.findOne(var1));
    }

    @Override
    @Transactional
    public synchronized boolean exists(String var1)
    {
        return repository.exists(var1);
    }

    @Override
    @Transactional
    public synchronized Optional<List<ArtifactEntry>> findAll()
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
    public synchronized Optional<List<ArtifactEntry>> findAll(List<String> var1)
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
    public synchronized long count()
    {
        return repository.count();
    }

    @Override
    @Transactional
    public synchronized void delete(String var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(ArtifactEntry var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void delete(Iterable<? extends ArtifactEntry> var1)
    {
        repository.delete(var1);
    }

    @Override
    @Transactional
    public synchronized void deleteAll()
    {
        repository.deleteAll();
    }
}
