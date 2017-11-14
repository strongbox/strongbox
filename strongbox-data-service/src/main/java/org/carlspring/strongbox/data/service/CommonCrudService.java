package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.service.impl.EntityServiceRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
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

    // TODO: recursive identification + collections
    protected void cascadeEntityIdentification(T entity)
    {
        identifyEntity(entity);
        ReflectionUtils.doWithFields(entity.getClass(), (field)->{
            ReflectionUtils.makeAccessible(field);
            Class<? extends GenericEntity> t = (Class<? extends GenericEntity>) field.getType();
            if (!GenericEntity.class.isAssignableFrom(t))
            {
                return;
            }
            
            GenericEntity subEntity = (GenericEntity) ReflectionUtils.getField(field, entity);
            if (subEntity == null)
            {
                return;
            }

            CommonCrudService<GenericEntity> entityService = (CommonCrudService<GenericEntity>) entityServiceRegistry.getEntityService(subEntity.getClass());
            entityService.identifyEntity(subEntity);
        });
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
                                      entity.getClass().getSimpleName());

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
        cascadeEntityIdentification(entity);
        return getDelegate().save(entity);
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
        query = query.substring(0, query.length() - 5) + ";";

        // now query should looks like
        // SELECT * FROM Foo WHERE blah = :blah AND moreBlah = :moreBlah

        logger.debug("Executing SQL query> " + query);

        return query;
    }

    private String getEntityClassSimpleNameAsCamelHumpVariable()
    {
        String simpleName = getEntityClass().getSimpleName();

        simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());

        return simpleName;
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

}
