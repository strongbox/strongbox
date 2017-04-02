package org.carlspring.strongbox.data.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Transactional
public abstract class CommonCrudService<T extends GenericEntity> implements CrudService<T, String>
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <S extends T> S save(S entity)
    {
        if (entity.getObjectId() == null && entity.getUuid() == null)
        {
            entity.setUuid(UUID.randomUUID().toString());
        } else if (entity.getObjectId() == null && entity.getUuid() != null){
            String sQuery = String.format("select @rid as objectId from %s where uuid=:uuid", getEntityClass().getSimpleName());
            OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<ODocument>(sQuery);
            oQuery.setLimit(1);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("uuid", entity.getUuid());

            List<ODocument> resultList = getDelegate().command(oQuery).execute(params);
            if (resultList.size() > 0){
                ODocument record = (ODocument)resultList.iterator().next();
                ODocument value = record.field("objectId");
                entity.setObjectId(value.getIdentity().toString());
            }
        }
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
        return Optional.of(resultList);
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

    /**
     * We can get an internal OrientDB transaction API with this, which can be needed to execute some OrientDB queries,
     * for example.
     * 
     * @return
     */
    protected OObjectDatabaseTx getDelegate()
    {
        return (OObjectDatabaseTx) entityManager.getDelegate();
    }

    public abstract Class<T> getEntityClass();
}
