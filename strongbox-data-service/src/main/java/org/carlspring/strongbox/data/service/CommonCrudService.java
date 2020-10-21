package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.impl.EntityServiceRegistry;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

@Transactional
public abstract class CommonCrudService<T extends GenericEntity>
        implements CrudService<T, String>
{

    private static final Logger logger = LoggerFactory.getLogger(CommonCrudService.class);

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    private EntityServiceRegistry entityServiceRegistry;

    @PostConstruct
    public void postConstruct()
    {
        entityServiceRegistry.register(this.getEntityClass(), this);
    }

    protected <S extends T> S cascadeEntitySave(T entity)
    {
        identifyEntity(entity);
        
        ReflectionUtils.doWithFields(getEntityClass(), (field) -> {
            ReflectionUtils.makeAccessible(field);

            Set<CascadeType> cascadeTypeSet = Arrays.stream(field.getAnnotations())
                                                    .map(a -> exposeCascadeType(a))
                                                    .reduce((c1,
                                                             c2) -> {
                                                        c1.addAll(c2);
                                                        return c1;
                                                    })
                                                    .orElse(Collections.emptySet());
            
            if (!cascadeTypeSet.stream().anyMatch(c -> CascadeType.ALL.equals(c) || CascadeType.MERGE.equals(c)
                    || CascadeType.PERSIST.equals(c)))
            {
                return;
            }
            
            Class<?> fieldType = field.getType();
            Object fieldValue = ReflectionUtils.getField(field, entity);

            if (fieldValue == null)
            {
                return;
            }

            if (Collection.class.isAssignableFrom(fieldType))
            {
                Collection<Object> collection = (Collection<Object>) fieldValue;
                List<Object> replaceCollection = new LinkedList<>();
                collection.removeIf(a -> {
                    Object b = tryToCascadeEntitySave(a);
                    if (b != a)
                    {
                        replaceCollection.add(b);
                        return true;
                    }
                    return false;
                });
                collection.addAll(replaceCollection);
            }
            else
            {
                Object newFieldValue = tryToCascadeEntitySave(fieldValue);
                if (newFieldValue != fieldValue)
                {
                    ReflectionUtils.setField(field, entity, newFieldValue);
                }
            }

        });

        return getDelegate().save(entity);
    }

    private Set<CascadeType> exposeCascadeType(Annotation a)
    {
        Set<CascadeType> result = new HashSet<>();
        if (a instanceof OneToMany)
        {
            result.addAll(Arrays.asList(((OneToMany) a).cascade()));
        }
        else if (a instanceof OneToOne)
        {
            result.addAll(Arrays.asList(((OneToOne) a).cascade()));
        }
        else if (a instanceof ManyToMany)
        {
            result.addAll(Arrays.asList(((ManyToMany) a).cascade()));
        }
        else if (a instanceof ManyToOne)
        {
            result.addAll(Arrays.asList(((ManyToOne) a).cascade()));
        }
        return result;
    }

    protected Object tryToCascadeEntitySave(Object entityCandidate)
    {
        if (!(entityCandidate instanceof GenericEntity))
        {
            return entityCandidate;
        }

        GenericEntity entity = (GenericEntity) entityCandidate;
        CommonCrudService<GenericEntity> entityService = (CommonCrudService<GenericEntity>) entityServiceRegistry.getEntityService(entity.getClass());
        return entityService.cascadeEntitySave(entity);
    }
    
    protected boolean identifyEntity(T entity)
    {
        if (entity.getObjectId() != null)
        {
            return true;
        }
        else if (entity.getUuid() == null)
        {
            entity.setUuid(UUID.randomUUID().toString());
            return false;
        }
        
        String sQuery = String.format("SELECT @rid AS objectId FROM %s WHERE uuid = :uuid",
                                      getEntityClass().getSimpleName());

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", entity.getUuid());

        List<ODocument> resultList = getDelegate().command(oQuery).execute(params);
        if (resultList.isEmpty())
        {
            return false;
        }
        
        ODocument record = resultList.iterator().next();
        ODocument value = record.field("objectId");
        entity.setObjectId(value.getIdentity().toString());

        return true;
    }

    @Override
    public <S extends T> S save(S entity)
    {
        return cascadeEntitySave(entity);
    }

    @Override
    public T lockOne(String id)
    {
        String sQuery = String.format("SELECT * FROM %s LOCK RECORD", id);
        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<T> resultList = getDelegate().command(oQuery).execute();

        return !resultList.isEmpty() ? resultList.iterator().next() : null;
    }

    @Override
    public Optional<T> findOne(String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable((T) entityManager.find(getEntityClass(), id));
    }

    @Override
    public boolean existsByUuid(String uuid)
    {
        String sQuery = String.format("SELECT @rid FROM INDEX:idx_uuid WHERE key = :uuid");

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", uuid);

        List<ODocument> resultList = getDelegate().command(oQuery).execute(params);
        return !resultList.isEmpty();
    }

    @Override
    public boolean exists(String id)
    {
        return findOne(id) != null;
    }

    @Override
    public Optional<List<T>> findAll()
    {
        List<T> resultList = new ArrayList<>();
        for (T t : getDelegate().browseClass(getEntityClass()))
        {
            resultList.add(t);
        }

        return Optional.ofNullable(resultList.isEmpty() ? null : resultList);
    }

    @Override
    public long count()
    {
        return getDelegate().countClass(getEntityClass());
    }

    @Override
    public void delete(String id)
    {
        getDelegate().delete(new ORecordId(id));
    }

    @Override
    public void delete(T entity)
    {
        entityManager.remove(entity);
    }

    @Override
    public int delete(List<T> entityList)
    {
        if (entityList == null || entityList.isEmpty())
        {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(getEntityClass().getSimpleName()).append(" WHERE uuid in :uuids");

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("uuids", entityList.stream().map(GenericEntity::getUuid).collect(Collectors.toList()));

        OCommandSQL oCommandSQL = new OCommandSQL(sb.toString());
        return getDelegate().command(oCommandSQL).execute(parameterMap);
    }
    
    @Override
    public void deleteAll()
    {
        Optional<List<T>> findAll = findAll();
        if (!findAll.isPresent())
        {
            return;
        }

        for (T entity : findAll.get())
        {
            delete(entity);
        }
    }

    protected String buildQuery(Map<String, String> map)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(getEntityClass().getSimpleName());

        if (map == null || map.isEmpty())
        {
            return sb.toString();
        }

        sb.append(" WHERE ");

        // process only coordinates with non-null values
        map.entrySet()
           .stream()
           .filter(entry -> entry.getValue() != null)
           .forEach(entry -> sb.append(entry.getKey())
                               .append(" = :")
                               .append(entry.getKey())
                               .append(" AND "));

        // remove last 'and' statement (that doesn't relate to any value)
        String query = sb.toString();
        query = query.substring(0, query.length() - 5);

        // now query should looks like
        // SELECT * FROM Foo WHERE blah = :blah AND moreBlah = :moreBlah

        logger.debug("Executing SQL query> {}", query);

        return query;
    }

    private String getEntityClassSimpleNameAsCamelHumpVariable()
    {
        String simpleName = getEntityClass().getSimpleName();

        simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());

        return simpleName;
    }

    protected void appendPagingCriteria(StringBuilder queryBuilder,
                                        PagingCriteria pagingCriteria)
    {

        queryBuilder.append(String.format(" ORDER BY %s", pagingCriteria.getSort()));

        if (pagingCriteria.getSkip() > 0)
        {
            queryBuilder.append(String.format(" SKIP %s", pagingCriteria.getSkip()));
        }
        if (pagingCriteria.getLimit() > 0)
        {
            queryBuilder.append(String.format(" LIMIT %s", pagingCriteria.getLimit()));
        }
    }

    /**
     * We can get an internal OrientDB transaction API with this, which can be
     * needed to execute some OrientDB queries,
     * for example.
     *
     * @return
     */
    protected OObjectDatabaseTx getDelegate()
    {
        return (OObjectDatabaseTx) entityManager.getDelegate();
    }
    
    protected T detach(T entity)
    {
        return getDelegate().detachAll(entity, true);
    }

}
