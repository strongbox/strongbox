package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.lang3.time.DateUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link ArtifactEntry} entities.
 *
 * @author Sergey Bespalov
 */
@Service
@Transactional
class ArtifactEntryServiceImpl extends AbstractArtifactEntryService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryService.class);

    private boolean artifactEntryIsSavedForTheFirstTime(ArtifactEntry artifactEntry)
    {
        return artifactEntry.getUuid() == null;
    }

    @Override
    protected <S extends ArtifactEntry> S cascadeEntitySave(ArtifactEntry entity)
    {
        entity.setArtifactCoordinates(entity.getArtifactCoordinates());
        if (artifactEntryIsSavedForTheFirstTime(entity))
        {
            entity.setCreated(new Date());
        }

        return super.cascadeEntitySave(entity);
    }

    @Override
    public List<ArtifactEntry> findArtifactList(String storageId,
                                                String repositoryId,
                                                Map<String, String> coordinates,
                                                boolean strict)
    {
        return findArtifactList(storageId, repositoryId, coordinates, Collections.emptySet(), 0, -1, null, strict);
    }

    @Override
    @Transactional
    public List<ArtifactEntry> findArtifactList(String storageId,
                                                String repositoryId,
                                                Map<String, String> coordinates,
                                                Set<ArtifactTag> tagSet,
                                                int skip,
                                                int limit,
                                                String orderBy,
                                                boolean strict)
    {
        if (orderBy == null)
        {
            orderBy = "uuid";
        }

        coordinates = prepareParameterMap(coordinates, strict);

        Map<String, ArtifactTagEntry> tagMap = tagSet.stream()
                                                     .collect(Collectors.toMap(t -> String.format("%sTag", t.getName().replaceAll("-", "")),
                                                                               t -> (ArtifactTagEntry) t));

        String sQuery = buildCoordinatesQuery(toList(storageId, repositoryId), coordinates.keySet(), tagMap.keySet(),
                                              skip,
                                              limit, orderBy, strict);
        OSQLSynchQuery<ArtifactEntry> oQuery = new OSQLSynchQuery<>(sQuery);

        Map<String, Object> parameterMap = new HashMap<>(coordinates);
        if (storageId != null && !storageId.trim().isEmpty())
        {
            parameterMap.put("storageId0", storageId);
        }
        if (repositoryId != null && !repositoryId.trim().isEmpty())
        {
            parameterMap.put("repositoryId0", repositoryId);
        }

        tagMap.entrySet().stream().forEach(e -> parameterMap.put(e.getKey(), e.getValue().getName()));

        List<ArtifactEntry> entries = getDelegate().command(oQuery).execute(parameterMap);

        return entries;
    }

    @Override
    public List<ArtifactEntry> findMatching(ArtifactEntrySearchCriteria searchCriteria,
                                            PagingCriteria pagingCriteria)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT FROM ").append(getEntityClass().getSimpleName());
        Map<String, Object> parameterMap = Collections.emptyMap();


        if (!searchCriteria.isEmpty())
        {
            StringBuilder criteriaQueryClasuse = new StringBuilder();
            sb.append(" WHERE ");
            parameterMap = new HashMap<>();

            if (searchCriteria.getMinSizeInBytes() != null && searchCriteria.getMinSizeInBytes() > 0)
            {
                criteriaQueryClasuse.append(" sizeInBytes >= :minSizeInBytes ");
                parameterMap.put("minSizeInBytes", searchCriteria.getMinSizeInBytes());
            }
            if (searchCriteria.getLastAccessedTimeInDays() != null && searchCriteria.getLastAccessedTimeInDays() > 0)
            {
                if (criteriaQueryClasuse.length() > 0)
                {
                    criteriaQueryClasuse.append(" AND ");
                }
                Date lastUsed = DateUtils.addDays(new Date(), -searchCriteria.getLastAccessedTimeInDays());
                criteriaQueryClasuse.append(" lastUsed < :lastUsed ");
                parameterMap.put("lastUsed", lastUsed);
            }

            sb.append(criteriaQueryClasuse);
        }

        appendPagingCriteria(sb, pagingCriteria);

        logger.debug("Executing SQL query> {}", sb);

        OSQLSynchQuery<ArtifactEntry> oQuery = new OSQLSynchQuery<>(sb.toString());

        return getDelegate().command(oQuery).execute(parameterMap);
    }

    @Override
    public List<ArtifactEntry> findArtifactList(String storageId,
                                                String repositoryId,
                                                ArtifactCoordinates coordinates)
    {
        if (coordinates == null)
        {
            return findArtifactList(storageId, repositoryId, new HashMap<>(), true);
        }
        return findArtifactList(storageId, repositoryId, coordinates.getCoordinates(), true);
    }

    @Override
    public Long countCoordinates(Collection<Pair<String, String>> storageRepositoryPairList,
                                 Map<String, String> coordinates,
                                 boolean strict)
    {
        coordinates = prepareParameterMap(coordinates, strict);
        String sQuery = buildCoordinatesQuery(storageRepositoryPairList, coordinates.keySet(), Collections.emptySet(), 0, 0, null, strict);
        sQuery = sQuery.replace("*", "count(distinct(artifactCoordinates))");
        OSQLSynchQuery<ArtifactEntry> oQuery = new OSQLSynchQuery<>(sQuery);

        Map<String, Object> parameterMap = new HashMap<>(coordinates);

        Pair<String, String>[] p = storageRepositoryPairList.toArray(new Pair[storageRepositoryPairList.size()]);
        IntStream.range(0, storageRepositoryPairList.size()).forEach(idx -> {
            String storageId = p[idx].getValue0();
            String repositoryId = p[idx].getValue1();

            if (storageId != null && !storageId.trim().isEmpty())
            {
                parameterMap.put(String.format("storageId%s", idx), p[idx].getValue0());
            }
            if (repositoryId != null && !repositoryId.trim().isEmpty())
            {
                parameterMap.put(String.format("repositoryId%s", idx), p[idx].getValue1());
            }
        });


        List<ODocument> result = getDelegate().command(oQuery).execute(parameterMap);
        return (Long) result.iterator().next().field("count");
    }

    @Override
    public Long countArtifacts(Collection<Pair<String, String>> storageRepositoryPairList,
                               Map<String, String> coordinates,
                               boolean strict)
    {
        coordinates = prepareParameterMap(coordinates, strict);
        String sQuery = buildCoordinatesQuery(storageRepositoryPairList, coordinates.keySet(), Collections.emptySet(), 0, 0, null, strict);
        sQuery = sQuery.replace("*", "count(*)");
        OSQLSynchQuery<ArtifactEntry> oQuery = new OSQLSynchQuery<>(sQuery);

        Map<String, Object> parameterMap = new HashMap<>(coordinates);

        Pair<String, String>[] p = storageRepositoryPairList.toArray(new Pair[storageRepositoryPairList.size()]);
        IntStream.range(0, storageRepositoryPairList.size()).forEach(idx -> {
            String storageId = p[idx].getValue0();
            String repositoryId = p[idx].getValue1();

            if (storageId != null && !storageId.trim().isEmpty())
            {
                parameterMap.put(String.format("storageId%s", idx), p[idx].getValue0());
            }
            if (repositoryId != null && !repositoryId.trim().isEmpty())
            {
                parameterMap.put(String.format("repositoryId%s", idx), p[idx].getValue1());
            }
        });


        List<ODocument> result = getDelegate().command(oQuery).execute(parameterMap);
        return (Long) result.iterator().next().field("count");
    }

    @Override
    public Long countArtifacts(String storageId,
                               String repositoryId,
                               Map<String, String> coordinates,
                               boolean strict)
    {
        return countArtifacts(toList(storageId, repositoryId), coordinates,
                              strict);
    }

    public List<Pair<String, String>> toList(String storageId,
                                             String repositoryId)
    {
        return Arrays.asList(new Pair[] { Pair.with(storageId, repositoryId) });
    }

    protected String buildCoordinatesQuery(Collection<Pair<String, String>> storageRepositoryPairList,
                                           Set<String> parameterNameSet,
                                           Set<String> tagNameSet,
                                           int skip,
                                           int limit,
                                           String orderBy,
                                           boolean strict)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(getEntityClass().getSimpleName());

        Pair<String, String>[] storageRepositoryPairArray = storageRepositoryPairList.toArray(new Pair[storageRepositoryPairList.size()]);
        // COORDINATES
        StringBuffer c1 = new StringBuffer();
        parameterNameSet.stream()
                        .forEach(e -> c1.append(c1.length() > 0 ? " AND " : "")
                                        .append("artifactCoordinates.coordinates.")
                                        .append(e)
                                        .append(".toLowerCase()")
                                        .append(strict ? " = " : " like ")
                                        .append(String.format(":%s", e)));
        sb.append(" WHERE ").append(c1.length() > 0 ? c1.append(" AND ").toString() : " true = true AND ");

        //REPOSITORIES
        StringBuffer c2 = new StringBuffer();
        IntStream.range(0, storageRepositoryPairList.size())
                 .forEach(idx -> c2.append(idx > 0 ? " OR " : "")
                                   .append(calculateStorageAndRepositoryCondition(storageRepositoryPairArray[idx], idx)));
        sb.append(c2.length() > 0 ? c2.toString() : "true");

        //TAGS
        tagNameSet.stream().forEach(t -> sb.append(String.format(" AND tagSet contains (name = :%s)", t)));

        //ORDER
        if ("uuid".equals(orderBy))
        {
            sb.append(" ORDER BY artifactCoordinates.uuid");
        }
        else if (orderBy != null && !orderBy.trim().isEmpty())
        {
            sb.append(String.format(" ORDER BY artifactCoordinates.coordinates.%s", orderBy));
        }

        //PAGE
        if (skip > 0)
        {
            sb.append(String.format(" SKIP %s", skip));
        }
        if (limit > 0)
        {
            sb.append(String.format(" LIMIT %s", limit));
        }

        // now query should looks like
        // SELECT * FROM Foo WHERE blah = :blah AND moreBlah = :moreBlah

        logger.debug("Executing SQL query> {}", sb);

        return sb.toString();
    }

    public String calculateStorageAndRepositoryCondition(Pair<String, String> storageRepositoryPairArray,
                                                         int idx)
    {
        StringBuffer result = new StringBuffer();
        String storageId = storageRepositoryPairArray.getValue0();
        String repositoryId = storageRepositoryPairArray.getValue1();
        if (storageId != null && !storageId.trim().isEmpty())
        {
            result.append(String.format("storageId = :storageId%s", idx));
        }
        if (result.length() > 0)
        {
            result.append(" AND ");
        }
        if (repositoryId != null && !repositoryId.trim().isEmpty())
        {
            result.append(String.format("repositoryId = :repositoryId%s", idx));
        }
        if (result.length() > 0)
        {
            result.insert(0, "(").append(")");
        }
        else
        {
            result.append(" true = true");
        }

        return result.toString();
    }

    private Map<String, String> prepareParameterMap(Map<String, String> coordinates,
                                                    boolean strict)
    {
        return coordinates.entrySet()
                          .stream()
                          .filter(e -> e.getValue() != null)
                          .collect(Collectors.toMap(Map.Entry::getKey,
                                                    e -> calculateParameterValue(e, strict)));
    }

    private String calculateParameterValue(Entry<String, String> e,
                                           boolean strict)
    {
        String result = e.getValue() == null ? null : e.getValue().toLowerCase();
        if (!strict)
        {
            result = "%" + result + "%";
        }
        return result;
    }

    @Override
    public boolean artifactExists(String storageId,
                                  String repositoryId,
                                  String path)
    {
        return findArtifactEntryId(storageId, repositoryId, path) != null;
    }

    @Override
    public ArtifactEntry findOneArtifact(String storageId,
                                         String repositoryId,
                                         String path)
    {
        ORID artifactEntryId = findArtifactEntryId(storageId, repositoryId, path);
        return Optional.ofNullable(artifactEntryId)
                       .flatMap(id -> Optional.ofNullable(entityManager.find(ArtifactEntry.class, id)))
                       .map(e -> detach(e))
                       .orElse(null);
    }

    @Override
    public void delete(String id)
    {
        super.delete(id);
    }

    @Override
    public void delete(ArtifactEntry entity)
    {
        super.delete(entity);
    }

    @Override
    public void deleteAll()
    {
        super.deleteAll();
    }

    private ORID findArtifactEntryId(String storageId,
                                     String repositoryId,
                                     String path)
    {
        String sQuery = String.format("SELECT FROM INDEX:idx_artifact_coordinates WHERE key = :path");

        HashMap<String, Object> params = new HashMap<>();
        params.put("path", path);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<ODocument> resultList = getDelegate().command(oQuery).execute(params);
        ODocument result = resultList.isEmpty() ? null : resultList.iterator().next();

        ORID artifactCoordinatesId = result == null ? null : ((ODocument) result.field("rid")).getIdentity();
        if (artifactCoordinatesId == null)
        {
            return null;
        }

        sQuery = String.format("SELECT FROM INDEX:idx_artifact WHERE key = [:storageId, :repositoryId, :artifactCoordinatesId]");

        oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        params = new HashMap<>();
        params.put("storageId", storageId);
        params.put("repositoryId", repositoryId);
        params.put("artifactCoordinatesId", artifactCoordinatesId);

        resultList = getDelegate().command(oQuery).execute(params);
        result = resultList.isEmpty() ? null : resultList.iterator().next();

        return result == null ? null : ((ODocument) result.field("rid")).getIdentity();
    }

    @Override
    public Class<ArtifactEntry> getEntityClass()
    {
        return ArtifactEntry.class;
    }

    @Override
    protected ArtifactEntry detach(ArtifactEntry entity)
    {
        ArtifactEntry result = super.detach(entity);
        result.setArtifactCoordinates(getDelegate().detachAll(entity.getArtifactCoordinates(), true));

        return result;
    }



}
